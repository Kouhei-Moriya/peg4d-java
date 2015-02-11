package org.peg4d.infer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.peg4d.Grammar;
import org.peg4d.GrammarFactory;
import org.peg4d.Main;
import org.peg4d.ParsingContext;
import org.peg4d.ParsingObject;
import org.peg4d.ParsingSource;

public class Engine {
	private final boolean verbose;
	private final Grammar grammar;
	private final String grammarFileName;
	private final FormatFactory factory;

	private final double clusterTolerance;
	private final double maxMass;
	private final double minCoverage;

	public Engine(String grammarFileName, boolean verbose) {
		this.grammar = new GrammarFactory().newGrammar("main", grammarFileName, null);
		this.grammarFileName = grammarFileName;
		this.factory = new FormatFactory(this);
		this.verbose = verbose;

		this.clusterTolerance = 0.01;
		this.maxMass = 0.01;
		this.minCoverage = 0.9;
	}

	public void calcCorrectness(String filePath) {
		int successCount = 0;
		List<Chunk> chunks = this.chunking(org.peg4d.ParsingSource.loadSource(filePath));
		for (Chunk chunk : chunks) {
			ParsingSource source = chunk.baseSource;
			ParsingContext context = new ParsingContext(source);
			ParsingObject po = context.parse2(this.grammar, "Chunk", new ParsingObject(), null);
			if(context.isFailure()) {
				//pass
			}
			else {
				successCount++;
			}
		}
		System.out.println((float)successCount / chunks.size());
	}
	
	public Format infer(String filePath) {
		ParsingSource source = org.peg4d.ParsingSource.loadSource(filePath);
		List<Chunk> chunks = this.chunking(source);
		List<TokenizedChunk> tokenizedChunks = this.tokenize(chunks);
		Format fmt = this.structureDiscovery(new Branch(tokenizedChunks));
		fmt = this.formatRefinement(fmt);
		return fmt;
	}
	public Format infer(String filePath, Statistic stat) {
		ParsingSource source = org.peg4d.ParsingSource.loadSource(filePath);
		stat.beginChunking();
		List<Chunk> chunks = this.chunking(source);
		stat.endChunking();
		stat.beginTokenize();
		List<TokenizedChunk> tokenizedChunks = this.tokenize(chunks);
		stat.endTokenize();
		stat.beginStructureDiscovery();
		Format fmt = this.structureDiscovery(new Branch(tokenizedChunks));
		stat.endStructureDiscovery();
		stat.beginFormatRefinement();
		fmt = this.formatRefinement(fmt);
		stat.endFormatRefinement();
		return fmt;
	}
	
	List<Chunk> chunking(ParsingSource source) { //by linefeeds
		List<Chunk> ret = new ArrayList<>();
		long startPos = 0, endPos = 0, length = source.length();
		while (startPos < length) {
			while (endPos < length) {
				int ch = source.byteAt(endPos);
				if(ch == '\n') break;
				endPos = endPos + 1;
			}
			if (startPos != endPos) {
				ret.add(new Chunk(source.substring(startPos, endPos)));
			}
			startPos = endPos + 1;
			endPos = endPos + 1;
		}
		return ret;
	}
	
	List<TokenizedChunk> tokenize(List<Chunk> chunks) {
		List<TokenizedChunk> ret = new ArrayList<>();
		for (Chunk chunk : chunks) {
			ret.add(chunk.tokenize(this.grammar));
		}
		return ret;
	}

	Format structureDiscovery(Branch inputBranch) {
		Format ret = null;
		List<Histogram> histograms = this.createHistograms(inputBranch);
		if (histograms == null) {
			ret = this.factory.create();
		}
		else {
			List<List<Histogram>> groups = this.groupHistogram(histograms);
			Set<Token<?>> delimiters = this.findDelimiters(groups);
			if (delimiters != null) {
				Splitter splitter = new Splitter(delimiters);
				SplittedBranch splittedBranch = splitter.splitBranch(inputBranch);
				ret = this.factory.create(splittedBranch, splitter);
			}
			else {
				List<Branch> groupedBranches = this.groupChunks(inputBranch);
				ret = this.factory.create(groupedBranches);
			}
		}
		return ret;
	}
	private List<Histogram> createHistograms(Branch branch) {
		Map<Token<?>, Map<Integer, Integer>> tokenCounterV = new HashMap<>();
		Map<Token<?>, Integer> tokenCounterH = null;
		for (TokenizedChunk chunk : branch) {
			tokenCounterH = new HashMap<>();
			for (SimpleToken token : chunk.getSimpleTokenList()) {
				tokenCounterH.compute(token, (key, old) -> old != null ? Integer.valueOf(++old) : Integer.valueOf(1));
			}
			for (MetaToken token : chunk.getMetaTokenList()) {
				tokenCounterH.compute(token, (key, old) -> old != null ? Integer.valueOf(++old) : Integer.valueOf(1));				
			}
			tokenCounterH.forEach((token, tokenFrequency) -> {
				tokenCounterV.compute(token, (token_, map) -> {
					if (map == null) map = new HashMap<Integer, Integer>();
					map.compute(tokenFrequency, (tokenFrequency_, old) -> old != null ? Integer.valueOf(++old) : Integer.valueOf(1));
					return map;
				});
			});
		}
		
		List<Histogram> histograms = null;
		if (tokenCounterV.size() != 0) {
			histograms = new ArrayList<>();
			Histogram histogram = null;
			for (Map.Entry<Token<?>, Map<Integer, Integer>> kv : tokenCounterV.entrySet()) {
				histogram = new Histogram(kv.getKey(), kv.getValue(), branch.size());
				histograms.add(histogram);
			}
		}
		return histograms;
	}
	private List<List<Histogram>> groupHistogram(List<Histogram> histograms) {
		List<List<Histogram>> groups = new ArrayList<>();
		boolean grouped;
		for (Histogram histogram : histograms) {
			grouped = false;
			for (List<Histogram> group : groups) {
				if (Histogram.calcSimilarity(histogram, group.get(0)) < this.clusterTolerance) {
					group.add(histogram);
					grouped = true;
					break;
				}
			}
			if (!grouped) {
				List<Histogram> newGroup = new ArrayList<>();
				newGroup.add(histogram);
				groups.add(newGroup);
			}
		}
		return groups;
	}
	private Set<Token<?>> findDelimiters(List<List<Histogram>> groups) {
		List<Histogram> delimGroup = null;
		Histogram rep = null;
		double rmOfRet = 0;
		for (List<Histogram> group : groups) {
			rep = group.get(0);
			double rmOfRep = rep.residualMass(0);
			for (int i = 1; i < group.size(); i++) {
				double tmp = group.get(i).residualMass(0);
				if (rmOfRep > tmp) {
					rep = group.get(i);
					rmOfRep = tmp;
				}
			}
			if (rmOfRep < (this.maxMass * rep.wholeSize()) && rep.coverage() > this.minCoverage) {
				if (delimGroup != null) {
					double minor = Math.min(rmOfRet, rmOfRep);
					if (rmOfRep == minor) delimGroup = group;
				}
				else {
					delimGroup = group;
				}
			}
		}
		Set<Token<?>> ret = null;
		if (delimGroup != null) {
			ret = new HashSet<>();
			for (Histogram h : delimGroup) {
				ret.add(h.baseToken);
			}
		}
		return ret;
	}
	private List<Branch> groupChunks(Branch branch) {
		Map<Token<?>, Branch> mappedBranches = new HashMap<>();
		Token<?> keyToken = null;
		for (TokenizedChunk chunk : branch) {
			if (chunk.size() == 0) {
				keyToken = null;
			}
			else {
				keyToken = chunk.getSimpleTokenList().get(0);
			}
			mappedBranches.compute(keyToken, (key, value) -> {
				if (value == null) value = new Branch();
				value.add(chunk);
				return value;
			});
		}
		return new ArrayList<>(mappedBranches.values());
	}
	
	Format formatRefinement(Format fmt) {
		fmt = fmt.refine();
		fmt.rename(0);
		return fmt;
	}
	private List<CollectionFormat> collectListFormat(Format fmt) {
		List<CollectionFormat> ret = new ArrayList<>();
		CollectionFormat tmp = null;
		if (fmt instanceof CollectionFormat) {
			tmp = (CollectionFormat)fmt;
			ret.add(tmp);
			for (Format inner : tmp) {
				ret.addAll(collectListFormat(inner));
			}
		}
		return ret;
	}
	
	public void outputNez(String outputFileName, Format fmt) {
		StringBuilder builder = new StringBuilder();
		builder.append("File\n  = {#File (@Chunk \"\\n\"?)+}\n");
		builder.append("Chunk\n  = {#Chunk format0}\n");
		this.output(outputFileName, fmt, builder.toString());
	}
	public void outputPegjs(String outputFileName, Format fmt) {
		StringBuilder builder = new StringBuilder();
		builder.append("");
		builder.append("start = File / Chunk\n");
		builder.append("File\n  = (Chunk \"\\n\"?)+\n");
		builder.append("Chunk\n  = f:format0 {console.log(f)}\n");
		this.output(outputFileName, fmt, builder.toString());
	}
	
	private void output(String outputFileName, Format fmt, String preface) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
			writer.write(preface);
			for (CollectionFormat col : collectListFormat(fmt)) {
				writer.write(col.getDefinition() + "\n");
			}
			BufferedReader reader = new BufferedReader(new FileReader(this.grammarFileName));
			int blockSize = 1024, readSize = 0;
			char[] block = new char[blockSize];
			while ((readSize = reader.read(block, 0, blockSize)) != -1) {
				writer.write(block, 0, readSize);
			}
			reader.close();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			Main._Exit(1, "output file error : " + outputFileName);
		}
	}
	
	public void output(PrintStream stream, Format fmt) {
		for (CollectionFormat lfmt : collectListFormat(fmt)) {
			System.out.println(lfmt.getDefinition());
		}
	}
}
