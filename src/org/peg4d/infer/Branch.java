package org.peg4d.infer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Branch implements Iterable<TokenizedChunk> {
	List<TokenizedChunk> chunks;
	
	Branch() {
		this.chunks = new ArrayList<>();
	}
	Branch(List<TokenizedChunk> chunks) {
		this.chunks = chunks;
	}

	void add(TokenizedChunk chunk) {
		this.chunks.add(chunk);
	}
	int size() {
		return this.chunks.size();
	}
	TokenizedChunk get(int n) {
		return this.chunks.get(n);
	}
	
	@Override
	public Iterator<TokenizedChunk> iterator() {
		return this.chunks.iterator();
	}
}

class SplittedBranch extends Branch {
	private List<Token<?>> delimSeq;
	private List<List<Token<?>>> delimSrc;
	private List<? extends Branch> subBranches;
	private Direction direction;
	
	enum Direction {
		Horizontal,
		Vertical,
	}
	
	public SplittedBranch(List<SplittedChunk> splittedChunks, List<Token<?>> delimSeq) { // for vertical split
		this.subBranches = Util.newListOfBranch(Splitter.delimSeq2SplittedSize(delimSeq));
		this.delimSrc = Util.newListOfList(delimSeq.size());
		this.delimSeq = delimSeq;
		this.direction = Direction.Vertical;
		TokenizedChunk[] subChunks = null;
		for (SplittedChunk chunk : splittedChunks) {
			subChunks = chunk.getSubChunks();
			delimSeq = chunk.getDelimSeq();
			if (!delimSeq.equals(this.delimSeq)) throw new RuntimeException();
			for (int i = 0; i < subChunks.length; i++) {
				subBranches.get(i).add(subChunks[i]);
			}
			for (int i = 0; i < delimSrc.size(); i++) {
				delimSrc.get(i).add(delimSeq.get(i));
			}
		}
	}
	public SplittedBranch(List<SplittedBranch> subBranches) { // for horizontal split
		this.delimSeq = null;
		this.delimSrc = null;
		this.subBranches = subBranches;
		this.direction = Direction.Horizontal;
	}

	public Direction getDirection() {
		return direction;
	}
	public List<? extends Branch> getSubBranches() {
		return this.subBranches;
	}
	public List<Token<?>> getDelimSeq() {
		return this.delimSeq;
	}
	public List<List<Token<?>>> getDelimSrc() {
		return this.delimSrc;
	}
}
