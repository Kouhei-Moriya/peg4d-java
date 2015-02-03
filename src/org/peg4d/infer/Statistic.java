package org.peg4d.infer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.peg4d.Main;

public class Statistic {
	private long chunkingBegin;
	private long chunkingEnd;
	private long tokenizeBegin;
	private long tokenizeEnd;
	private long structureDiscoveryBegin;
	private long structureDiscoveryEnd;
	private long formatRefinementBegin;
	private long formatRefinementEnd;
	
	private long wholeTime;

	void beginChunking() {
		this.chunkingBegin = System.currentTimeMillis();
	}
	void endChunking() {
		this.chunkingEnd = System.currentTimeMillis();
	}
	void beginTokenize() {
		this.tokenizeBegin = System.currentTimeMillis();
	}
	void endTokenize() {
		this.tokenizeEnd = System.currentTimeMillis();
	}
	void beginStructureDiscovery() {
		this.structureDiscoveryBegin = System.currentTimeMillis();
	}
	void endStructureDiscovery() {
		this.structureDiscoveryEnd = System.currentTimeMillis();
	}
	void beginFormatRefinement() {
		this.formatRefinementBegin = System.currentTimeMillis();
	}
	void endFormatRefinement() {
		this.formatRefinementEnd = System.currentTimeMillis();
	}

	void output(String outputFileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
			writer.write(outputFileName);
			writer.write(",");
			writer.write(String.valueOf(this.chunkingEnd - this.chunkingBegin));
			writer.write(",");
			writer.write(String.valueOf(this.tokenizeEnd - this.tokenizeBegin));
			writer.write(",");
			writer.write(String.valueOf(this.structureDiscoveryEnd - this.structureDiscoveryBegin));
			writer.write(",");
			writer.write(String.valueOf(this.formatRefinementEnd - this.formatRefinementBegin));
			writer.write(",");
			writer.write(String.valueOf(this.wholeTime));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			Main._Exit(1, "output file error : " + outputFileName);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(this.chunkingEnd - this.chunkingBegin));
		sb.append(",");
		sb.append(String.valueOf(this.tokenizeEnd - this.tokenizeBegin));
		sb.append(",");
		sb.append(String.valueOf(this.structureDiscoveryEnd - this.structureDiscoveryBegin));
		sb.append(",");
		sb.append(String.valueOf(this.formatRefinementEnd - this.formatRefinementBegin));
		sb.append(",");
		sb.append(String.valueOf(this.wholeTime));
		return sb.toString();
	}
	public long getWholeTime() {
		return wholeTime;
	}
	public void setWholeTime(long wholeTime) {
		this.wholeTime = wholeTime;
	}
}
