package org.peg4d.infer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class Format {
	public Format refine() {
		return this;
	}
	public int rename(int n) {
		return n;
	}
	public int size() {
		return 1;
	}
}

abstract class CollectionFormat extends Format implements Iterable<Format> {
	protected String name;
	protected Collection<Format> body;
	
	protected String delimiter;
	
	CollectionFormat(String name) {
		this.name = name;
	}
	CollectionFormat(String name, Collection<Format> body) {
		this.name = name;
		this.body = body;
	}
	public void add(Format fmt) {
		this.body.add(fmt);
	}
	public String getDefinition() {
		StringJoiner sj = new StringJoiner(this.delimiter);
		for (Format fmt : this.body) {
			sj.add(fmt.toString());
		}
		return this.name + "\n  = " + sj.toString();
	}
	public String toString() {
		return this.name;
	}
	@Override
	public Iterator<Format> iterator() {
		return this.body.iterator();
	}
	
	@Override
	public int rename(int n) {
		this.name = "format" + n;
		n += 1;
		for (Format fmt : this.body) {
			n = fmt.rename(n);
		}
		return n;
	}
	
	@Override
	public int size() {
		int ret = 0;
		for (Format fmt : this.body) {
			ret += fmt.size();
		}
		return ret;
	}
}

class SeqFormat extends CollectionFormat {
	SeqFormat(String name) {
		super(name);
		this.body = new ArrayList<>();
		this.delimiter = " ";
	}
	SeqFormat(String name, List<Format> body) {
		super(name, body);
		this.delimiter = " ";
	}

	Format get(int n) {
		return ((List<Format>)this.body).get(n);
	}
	
	@Override
	public Format refine() {
		SeqFormat seq = new SeqFormat(this.name);
		String acc = null;
		for (Format fmt : this.body) {
			fmt = fmt.refine();
			if (fmt instanceof EmptyFormat) {
				//pass
			}
			else if (fmt instanceof TerminalFormat) {
				if (acc == null) acc = "";
				acc += ((TerminalFormat)fmt).text; 
			}
			else {
				if (acc != null) seq.add(new TerminalFormat(acc));
				acc = null;
				seq.add(fmt);
			}
		}
		if (acc != null) seq.add(new TerminalFormat(acc));
		switch (seq.body.size()) {
		case 0:
			return EmptyFormat.getInstance();
		case 1:
			return seq.get(0);
		default:
			return seq;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		else if (!(obj instanceof SeqFormat)) {
			return false;
		}
		else {
			return this.body.equals(((SeqFormat)obj).body);
		}
	}
	
	@Override
	public int hashCode() {
		return this.body.hashCode();
	}
}

class ChoiceFormat extends CollectionFormat {
	ChoiceFormat(String name) {
		super(name);
		this.delimiter = "\n  / ";
		this.body = new HashSet<>();
	}
	@Override
	public String getDefinition() {
		StringJoiner sj = new StringJoiner(this.delimiter);
		List<Format> sorted = new ArrayList<>(this.body);
		sorted.sort((f1, f2) -> {
			int size1 = f1.size();
			int size2 = f2.size();
			if (size1 != size2) {
				return size2 - size1;
			}
			else if (f1 instanceof TerminalFormat && f2 instanceof TerminalFormat) {
				return ((TerminalFormat)f2).text.compareTo(((TerminalFormat)f1).text);
			}
			else {
				return 0;
			}
		});
		for (Format fmt : sorted) {
			sj.add(fmt.toString());
		}
		return this.name + "\n  = " + sj.toString();
	}

	public Format refine() {
		Format ret = null;
		if (this.body.size() == 1) {
			for (Format fmt : this.body) {
				ret = fmt.refine();
			}
		}
		else {
			ChoiceFormat choice = new ChoiceFormat(this.name);
			for (Format fmt : this.body) {
				fmt = fmt.refine();
				if (fmt instanceof CollectionFormat && ((CollectionFormat)fmt).body.size() == 0) {
					//pass
				}
				else {
					choice.add(fmt);
				}
			}
			if (choice.body.contains(EmptyFormat.getInstance())) {
				ret = new OptionFormat(choice.name, choice.body);
			}
			else {
				ret = choice.putOutPreffixOrSuffix();
			}
		}
		return ret;
	}
	Format putOutPreffixOrSuffix() {
		if (!(this.body.size() >= 2)) return this;
		List<Format> prefixes = new ArrayList<>();
		List<Format> suffixes = new ArrayList<>();
		List<List<Format>> bodies = new ArrayList<>();
		SeqFormat tmp = null;
		for (Format fmt : this.body) {
			if (fmt instanceof SeqFormat) {
				tmp = (SeqFormat)fmt;
				if (!(tmp.body.size() >= 2)) return this;
				prefixes.add(tmp.get(0));
				suffixes.add(tmp.get(tmp.body.size() - 1));
				bodies.add((List<Format>)tmp.body);
			}
			else {
				return this;
			}
		}
		Set<Format> prefixSet = new HashSet<Format>(prefixes);
		Set<Format> suffixSet = new HashSet<Format>(suffixes);
		boolean prefixExists = prefixSet.size() == 1;
		boolean suffixExists = suffixSet.size() == 1;
		SeqFormat ret = new SeqFormat(this.name);
		this.body.clear();
		for (List<Format> body : bodies) {
			int startPos = prefixExists ? 1 : 0;
			int endPos = body.size();
			if (suffixExists) endPos--;
			SeqFormat seqTmp = new SeqFormat(null, body.subList(startPos, endPos));
			if (seqTmp.body.size() != 0) this.body.add(seqTmp);
		}
		List<Format> body = bodies.get(0);
		if (prefixExists) ret.add(body.get(0));
		ret.add(this);
		if (suffixExists) ret.add(body.get(body.size() - 1));
		return ret;
	}
	
	@Override
	public int size() {
		int ret = 0, tmp = 0;
		for (Format fmt : this.body) {
			tmp = fmt.size();
			ret = ret > tmp ? ret : tmp;
		}
		return ret;
	}
}

class OptionFormat extends ChoiceFormat {
	OptionFormat(String name, Collection<Format> body) {
		super(name);
		body.remove(EmptyFormat.getInstance());
		this.body = body;
	}
	
	public String toString() {
		return this.name + "?";
	}
}

class TerminalFormat extends Format {
	String text;

	protected TerminalFormat() {
	}
	
	public TerminalFormat(String text) {
		if (text != null && !text.isEmpty()) {
			this.text = text;
		}
		else {
			throw new RuntimeException();
		}
	}

	@Override
	public String toString() {
		return "\"" + escape(this.text) + "\"";
	}
	
	static String escape(String text) {
		return text.replace("\"", "\\\"");
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		else if (!(obj instanceof TerminalFormat)) {
			return false;
		}
		else {
			TerminalFormat fmt = (TerminalFormat)obj;
			return this.text.equals(fmt.text);
		}
	}

	@Override
	public int hashCode() {
		return this.text.hashCode();
	}
	
	@Override
	public int size() {
		return this.text.length();
	}
}

class EmptyFormat extends TerminalFormat {
	public static final EmptyFormat instance = new EmptyFormat();
	
	private EmptyFormat() {
		super();
		this.text = "";
	}
	
	public static EmptyFormat getInstance() {
		return EmptyFormat.instance;
	}
	@Override
	public int size() {
		return 0;
	}
}

class NonTerminalFormat extends Format {
	String text;
	List<Token<?>> src;
	int variationMax;

	public NonTerminalFormat(String text) {
		if (text != null && !text.isEmpty()) {
			this.text = text;
			this.src = null;
		}
		else {
			throw new RuntimeException();
		}
	}
	public NonTerminalFormat(String text, List<Token<?>> src, int variationMax) {
		this(text);
		this.src = src;
		this.variationMax = variationMax;
	}

	@Override
	public String toString() {
		return this.text;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		else if (!(obj instanceof NonTerminalFormat)) {
			return false;
		}
		else {
			NonTerminalFormat fmt = (NonTerminalFormat)obj;
			return this.text.equals(fmt.text);
		}
	}

	@Override
	public int hashCode() {
		return this.text.hashCode();
	}

	@Override
	public Format refine() {
		if (this.src != null) {
			ChoiceFormat ret = new ChoiceFormat(null);
			for (Token<?> token : this.src) {
				ret.add(new TerminalFormat(token.getValue()));
			}
			if (ret.body.size() < this.variationMax) {
				return ret.refine();
			}
		}
		return this;
	}
}