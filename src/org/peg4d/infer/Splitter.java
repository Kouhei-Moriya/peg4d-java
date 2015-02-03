package org.peg4d.infer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Splitter {
	private Set<SimpleToken> simpleDelimSet;
	private Set<MetaToken> metaDelimSet;

	Splitter(Set<Token<?>> delimiters) {
		this.metaDelimSet = new HashSet<>();
		this.simpleDelimSet = new HashSet<>();
		for (Token<?> delimiter : delimiters) {
			if (delimiter instanceof MetaToken) {
				this.metaDelimSet.add((MetaToken)delimiter);
			}
			else if (delimiter instanceof SimpleToken) {
				this.simpleDelimSet.add((SimpleToken)delimiter);
			}
			else {
				throw new RuntimeException("Structure Discovery error : unknown Token " + delimiter);
			}
		}
	}
	
	public SplittedBranch splitBranch(Branch branch) {
		if (branch instanceof SplittedBranch) return (SplittedBranch)branch;
		Map<List<Token<?>>, List<SplittedChunk>> map = new HashMap<>();
		SplittedChunk splittedChunk = null;
		List<Token<?>> delimSeq = null;
		for (TokenizedChunk chunk : branch) {
			splittedChunk = this.splitChunk(chunk);
			delimSeq = splittedChunk.getDelimSeq();
			if (!map.containsKey(delimSeq)) map.put(delimSeq, new ArrayList<>());
			map.get(delimSeq).add(splittedChunk);
		}
		switch (map.size()) {
		case 0:
			return null;
		case 1:
			delimSeq = splittedChunk.getDelimSeq();
			return new SplittedBranch(map.get(delimSeq), delimSeq);
		default:
			List<SplittedBranch> subBranches = new ArrayList<>();
			for (Map.Entry<List<Token<?>>, List<SplittedChunk>> kv : map.entrySet()) {
				subBranches.add(new SplittedBranch(kv.getValue(), kv.getKey()));
			}
			return new SplittedBranch(subBranches);
		}
	}
	
	static public int delimSeq2SplittedSize(List<Token<?>> delimSeq) {
		int ret = 1;
		for (Token<?> delimiter : delimSeq) {
			if (delimiter instanceof MetaToken) {
				ret += 2;
			}
			else if (delimiter instanceof SimpleToken) {
				ret += 1;
			}
			else {
				throw new RuntimeException("Structure Discovery error : unknown Token " + delimiter);
			}
		}
		return ret;
	}
	
	public SplittedChunk splitChunk(TokenizedChunk chunk) {
		SplittedChunk ret = null;
		if (this.metaDelimSet.size() > 0) {
			ret = this.splitChunkByMetaToken(chunk);
		}
		else if (this.simpleDelimSet.size() > 0) {
			ret = this.splitChunkBySimpleToken(chunk);
		}
		return ret;
	}
	private SplittedChunk splitChunkByMetaToken(TokenizedChunk chunk) {
		List<Token<?>> delimSeq = new ArrayList<>();
		for (MetaToken token : chunk.getMetaTokenList()) {
			if (this.metaDelimSet.contains(token)) delimSeq.add(token);
		}
		int i = 0;
		long lastPos = chunk.getStartPos();
		TokenizedChunk[] splittedChunk = new TokenizedChunk[2 * delimSeq.size() + 1];
		for (Token<?> delim = null; i < delimSeq.size(); i++) {
			delim = delimSeq.get(i);
			splittedChunk[i * 2] = chunk.subChunk(lastPos, delim.getStartPos());
			splittedChunk[i * 2 + 1] = chunk.subChunk(delim.getStartPos() + 1, delim.getEndPos() - 1);
			lastPos = delim.getEndPos();
		}
		splittedChunk[i * 2] = chunk.subChunk(lastPos, chunk.getEndPos());
		return new SplittedChunk(chunk, delimSeq, splittedChunk);
	}
	private SplittedChunk splitChunkBySimpleToken(TokenizedChunk chunk) {
		List<Token<?>> delimSeq = new ArrayList<>();
		for (SimpleToken token : chunk.getSimpleTokenList()) {
			if (this.simpleDelimSet.contains(token)) delimSeq.add(token);
		}
		int i = 0;
		long lastPos = chunk.getStartPos();
		TokenizedChunk[] splittedChunk = new TokenizedChunk[delimSeq.size() + 1];
		for (Token<?> delim = null; i < delimSeq.size(); i++) {
			delim = delimSeq.get(i);
			splittedChunk[i] = chunk.subChunk(lastPos, delim.getStartPos());
			lastPos = delim.getEndPos();
		}
		splittedChunk[i] = chunk.subChunk(lastPos, chunk.getEndPos());
		return new SplittedChunk(chunk, delimSeq, splittedChunk);
	}
}