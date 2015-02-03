package org.peg4d.infer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.peg4d.Grammar;
import org.peg4d.ParsingContext;
import org.peg4d.ParsingRule;
import org.peg4d.ParsingSource;
import org.peg4d.UList;

public class Chunk {
	protected String text;
	
	protected ParsingSource baseSource;
	protected long startPos;
	protected long endPos;
	
	public Chunk(String text) {
		this.text = text;
		this.baseSource = org.peg4d.Utils.newStringSource("", 0, text);
		this.startPos = 0;
		this.endPos = text.length();
	}
	public Chunk(ParsingSource baseSource, long startPos, long endPos) {
		this.text = null;
		this.baseSource = baseSource;
		this.startPos = startPos;
		this.endPos = endPos;
	}

	public String getText() {
		if (this.text == null) {
			this.text = this.baseSource.substring(this.startPos, this.endPos);
		}
		return this.text;
	}

	public ParsingSource getBaseSource() {
		return this.baseSource;
	}
	
	public long getStartPos() {
		return this.startPos;
	}
	
	public long getEndPos() {
		return this.endPos;
	}
	
	public long size() {
		return this.endPos - this.startPos;
	}
	
	public TokenizedChunk tokenize(Grammar grammar) {
		return new TokenizedChunk(this, grammar);
	}
	protected List<SimpleToken> tokenizeForSimpleToken(Grammar grammar) {
		ParsingSource source = this.getBaseSource();
		ParsingContext context = new ParsingContext(source);
		List<SimpleToken> simpleTokenList = new ArrayList<>();
		UList<ParsingRule> ruleList = grammar.getExportRuleList();
		ParsingRule matchedPattern = null;
		long startPos = context.getPosition(), endPos = 0, length = source.length();
		while (startPos < length) {
			matchedPattern = null;
			for (ParsingRule rule : ruleList) {
				context.parse(grammar, rule.ruleName);
				if (!context.isFailure()) {
					matchedPattern = rule;
					break;
				}
			}
			if (matchedPattern == null) throw new RuntimeException("invalid tokenizer : cannot parse target");
			endPos = context.getPosition();
			simpleTokenList.add(SimpleToken.create(matchedPattern, startPos, endPos, source));
			startPos = endPos;
		}
		return simpleTokenList;
	}
	protected List<MetaToken> tokenizeForMetaToken() {
		List<MetaToken> metaTokenList = new ArrayList<>();
		Pattern startPattern = TokenizedChunk.metaTokenStartPattern;
		Map<String, Pattern> endPatternMap = TokenizedChunk.metaTokenEndPatternTable;
		String text = this.getText();
		Matcher startMatcher = startPattern.matcher(text), endMatcher = null;
		int startPos = 0;
		while (startMatcher.find(startPos)) {
			char c = startMatcher.group().charAt(0);
			switch (c) {
			case '[':
			case '"':
			case '{':
				endMatcher = endPatternMap.get(String.valueOf(c)).matcher(text);
				break;
			default:
				throw new RuntimeException("Tokenize error : not support meta token of " + c + " yet");
			}
			if (endMatcher.find(startMatcher.end())) {
				metaTokenList.add(new MetaToken(
						startMatcher.group() + "*" + endMatcher.group(),
						startMatcher.start(),
						endMatcher.end(),
						this.getBaseSource()));
				startPos = endMatcher.end();
			}
			else {
				startPos = startMatcher.end();
			}
		}
		return metaTokenList;
	}
}

class TokenizedChunk extends Chunk {
	private List<SimpleToken> simpleTokenList = null;
	private List<MetaToken> metaTokenList = null;
	
	public static final Pattern metaTokenStartPattern = Pattern.compile("[\\[\"{]");
	public static final Map<String, Pattern> metaTokenEndPatternTable = new HashMap<>();
	static {
		metaTokenEndPatternTable.put("\"", Pattern.compile("\""));
		metaTokenEndPatternTable.put("[", Pattern.compile("\\]"));
		metaTokenEndPatternTable.put("{", Pattern.compile("}"));
	}

	public void initialize(List<SimpleToken> simpleTokenList, List<MetaToken> metaTokenList) {
		this.simpleTokenList = simpleTokenList == null ? new ArrayList<>() : simpleTokenList;
		this.metaTokenList = metaTokenList == null ? new ArrayList<>() : metaTokenList;
	}
	
	public TokenizedChunk(String text) {
		super(text);
		this.initialize(null, null);
	}
	public TokenizedChunk(ParsingSource baseSource, long startPos, long endPos) {
		super(baseSource, startPos, endPos);
		this.initialize(null, null);
	}
	public TokenizedChunk(Chunk chunk, Grammar grammar) {
		super(chunk.baseSource, chunk.startPos, chunk.endPos);
		this.initialize(this.tokenizeForSimpleToken(grammar), this.tokenizeForMetaToken());
	}
	public TokenizedChunk(TokenizedChunk tokenizedChunk) {
		super(tokenizedChunk.baseSource, tokenizedChunk.startPos, tokenizedChunk.endPos);
		this.initialize(tokenizedChunk.simpleTokenList, tokenizedChunk.metaTokenList);
	}
	
	TokenizedChunk subChunk(long startPos, long endPos) {
		if (startPos >= this.startPos && endPos <= this.endPos) {
			TokenizedChunk subChunk = new TokenizedChunk(this.baseSource, startPos, endPos);
			if (this.simpleTokenList != null) {
				int listSize = this.simpleTokenList.size();
				int startIdx = listSize, endIdx = listSize;
				for (int i = 0; i < listSize; i++) {
					if (this.simpleTokenList.get(i).getStartPos() >= startPos) {
						startIdx = i;
						break;
					}
				}
				for (int i = startIdx; i < listSize; i++) {
					if (this.simpleTokenList.get(i).getEndPos() > endPos) {
						endIdx = i;
						break;
					}
				}
				subChunk.simpleTokenList = this.simpleTokenList.subList(startIdx, endIdx);
			}
			if (this.metaTokenList != null) {
				int listSize = this.metaTokenList.size();
				int startIdx = listSize, endIdx = listSize;
				for (int i = 0; i < listSize; i++) {
					if (this.metaTokenList.get(i).getStartPos() >= startPos) {
						startIdx = i;
						break;
					}
				}
				for (int i = startIdx; i < listSize; i++) {
					if (this.metaTokenList.get(i).getEndPos() >= endPos) {
						endIdx = i;
						break;
					}
				}
				subChunk.metaTokenList = this.metaTokenList.subList(startIdx, endIdx);
			}
			return subChunk;
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	public List<SimpleToken> getSimpleTokenList() {
		return simpleTokenList;
	}

	public List<MetaToken> getMetaTokenList() {
		return metaTokenList;
	}
	
	public SplittedChunk split(Set<Token<?>> delimiters) {
		return this.split(new Splitter(delimiters));
	}
	public SplittedChunk split(Splitter splitter) {
		return splitter.splitChunk(this);
	}
}

class SplittedChunk extends TokenizedChunk {
	private final List<Token<?>> delimSeq;
	private final TokenizedChunk[] subChunks;
	
	public SplittedChunk(TokenizedChunk base, List<Token<?>> delimSeq, TokenizedChunk[] subChunks) {
		super(base);
		this.delimSeq = delimSeq;
		this.subChunks = subChunks;
	}

	public List<Token<?>> getDelimSeq() {
		return delimSeq;
	}

	public TokenizedChunk[] getSubChunks() {
		return subChunks;
	}
}