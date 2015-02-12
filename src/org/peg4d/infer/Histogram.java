package org.peg4d.infer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Histogram {
	enum Form {
		ORDERED_BY_TOKEN_FREQUENCY,
		ORDERED_BY_COUNT_OF_CHUNKS,
	}
	final protected Token<?> baseToken;
	final protected int wholeSize; //whole size include 0 frequency
	final protected List<Bar> data; //exclude 0 frequency
	protected Form form = null;
	
	public Histogram(Token<?> baseToken, Map<Integer, Integer> resource, int size) {
		this(baseToken, resource, size, true);
	}
	public Histogram(Token<?> baseToken, Map<Integer, Integer> resource, int size, boolean doNormalize) {
		this.baseToken = baseToken;
		this.wholeSize = size;
		this.data = new ArrayList<>();
		resource.forEach((countOfChunks, tokenFrequency) -> {
			this.data.add(new Bar(countOfChunks, tokenFrequency));	
		});
		if (doNormalize) this.normalize();
	}
	public Histogram(Token<?> baseToken, List<Bar> data, int size) {
		this(baseToken, data, size, true);
	}
	public Histogram(Token<?> baseToken, List<Bar> data, int size, boolean doNormalize) {
		this.baseToken = baseToken;
		this.wholeSize = size;
		this.data = data;
		if (doNormalize) this.normalize();
	}

	protected int size() {
		return this.data.size();
	}
	protected int wholeSize() {
		return this.wholeSize;
	}
	
	public void normalize() {
		this.orderByTokenFrequency();
	}
	protected void orderByTokenFrequency() {
		if (this.form == Form.ORDERED_BY_TOKEN_FREQUENCY) return;
		this.data.sort((bar1, bar2) -> {
			int subOfTokenFrequency = bar2.tokenFrequency - bar1.tokenFrequency;
			int subOfCountOfChunks = bar2.countOfChunks - bar1.countOfChunks;
			if (subOfCountOfChunks == 0) {
				return subOfCountOfChunks;
			}
			else {
				return subOfTokenFrequency;
			}
		});
		this.form = Form.ORDERED_BY_TOKEN_FREQUENCY;
	}
	protected void orderByCountOfChunks() {
		if (this.form == Form.ORDERED_BY_COUNT_OF_CHUNKS) return;
		this.data.sort((bar1, bar2) -> {
			int subOfTokenFrequency = bar2.tokenFrequency - bar1.tokenFrequency;
			int subOfCountOfChunks = bar2.countOfChunks - bar1.countOfChunks;
			if (subOfTokenFrequency == 0) {
				return subOfTokenFrequency;
			}
			else {
				return subOfCountOfChunks;
			}
		});
		this.form = Form.ORDERED_BY_COUNT_OF_CHUNKS;
	}

	protected int getTokenFrequencyI(int idx) {
		return idx < this.size() ? this.data.get(idx).tokenFrequency : 0;
	}
	protected double getTokenFrequencyF(int idx) {
		return idx < this.size() ? this.data.get(idx).tokenFrequency : 0;
	}
	
	public double residualMass(int idx) {
		int rm = this.wholeSize();
		for (int i = idx; i >= 0; i--) {
			rm -= this.getTokenFrequencyI(i);
		}
		return (double)rm / this.wholeSize();
	}
	
	public double coverage() {
		double cov = 0;
		for (int i = 0; i < this.size(); i++) {
			cov += this.getTokenFrequencyI(i);
		}
		return cov / this.wholeSize();
	}
	
	public int width() {
		return this.size();
	}
	
	static protected double calcKLD(Histogram h1, Histogram h2) {
		double kld = 0;
		double v1, v2;
		for (int i = 0; i < h1.size(); i++) {
			v1 = h1.getTokenFrequencyF(i);
			v2 = h2.getTokenFrequencyF(i);
			kld += (v1 / h1.wholeSize()) * Math.log(v1 / v2);
		}
		return kld;
	}
	static public double calcSimilarity(Histogram h1, Histogram h2) {
		double sim = 0;
		Histogram ave = Histogram.average(h1, h2);
		sim = (Histogram.calcKLD(h1, ave) / 2) + (Histogram.calcKLD(h2, ave) / 2);
		return sim;
	}
	
	static public Histogram average(Histogram h1, Histogram h2) {
		List<Bar> newBody = new ArrayList<>();
		int[] sums = new int[Math.max(h1.size(), h2.size())];
		for (int i = 0; i < sums.length; i++) {
			sums[i] += h1.getTokenFrequencyI(i);
			sums[i] += h2.getTokenFrequencyI(i);
			newBody.add(new Bar(0, sums[i]));
		}
		return new AverageHistogram(null, newBody, Math.max(h1.wholeSize(), h2.wholeSize()), false);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (this.baseToken != null) {
			builder.append(this.baseToken);
		}
		else {
			builder.append("$average");
		}
		for (Bar bar : this.data) {
			builder.append("(");
			builder.append(bar.countOfChunks);
			builder.append(",");
			builder.append(bar.tokenFrequency);
			builder.append(")");
		}
		builder.append("c");
		builder.append(this.coverage());
		builder.append(",w");
		builder.append(this.width());
		return builder.toString();
	}
}

class AverageHistogram extends Histogram {
	public AverageHistogram(Token<?> baseToken, List<Bar> data, int size) {
		this(baseToken, data, size, true);
	}
	public AverageHistogram(Token<?> baseToken, List<Bar> data, int size, boolean doNormalize) {
		super(baseToken, data, size, doNormalize);
	}

	protected int getTokenFrequencyI(int idx) {
		return idx < this.size() ? (this.data.get(idx).tokenFrequency) / 2 : 0;
	}
	
	@Override
	protected double getTokenFrequencyF(int idx) {
		return idx < this.size() ? ((double)this.data.get(idx).tokenFrequency) / 2 : 0;
	}
}

class Bar {
	final int tokenFrequency;
	final int countOfChunks;
	Bar(int countOfChunks, int tokenFrequency) {
		this.tokenFrequency = tokenFrequency;
		this.countOfChunks = countOfChunks;
	}
}