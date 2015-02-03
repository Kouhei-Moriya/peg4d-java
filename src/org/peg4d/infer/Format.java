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
}

abstract class CollectionFormat extends Format implements Iterable<Format> {
	protected String name;
	protected Collection<Format> body;
	
	protected String delimiter;
	
	CollectionFormat(String name) {
		this.name = name;
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
}

class SeqFormat extends CollectionFormat {
	SeqFormat(String name) {
		super(name);
		this.body = new ArrayList<>();
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
			if (f1 instanceof TerminalFormat && f2 instanceof TerminalFormat) {
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
		if (this.body.size() == 1) {
			Format ret = null;
			for (Format fmt : this.body) {
				ret = fmt.refine();
			}
			return ret;
		}
		else {
			ChoiceFormat choice = new ChoiceFormat(this.name);
			for (Format fmt : this.body) {
				fmt = fmt.refine();
				choice.add(fmt);
			}
			if (choice.body.contains(EmptyFormat.getInstance())) {
				return new OptionFormat(choice.name, choice.body);
			}
			else {
				return choice;
			}
		}
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
}

class NonTerminalFormat extends Format {
	String text;
	List<Token<?>> src;

	public NonTerminalFormat(String text) {
		if (text != null && !text.isEmpty()) {
			this.text = text;
			this.src = null;
		}
		else {
			throw new RuntimeException();
		}
	}
	public NonTerminalFormat(String text, List<Token<?>> src) {
		this(text);
		this.src = src;
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
		if (this.src == null) {
			return this;	
		}
		else {
			ChoiceFormat ret = new ChoiceFormat(null);
			for (Token<?> token : this.src) {
				ret.add(new TerminalFormat(token.getValue()));
			}
			return ret.refine();
		}
	}
}