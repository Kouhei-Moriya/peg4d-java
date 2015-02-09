package org.peg4d.infer;

import java.util.List;

import org.peg4d.ParsingRule;
import org.peg4d.ParsingSource;

public class Token<T> {
	protected T type;
	protected String value;
	
	protected ParsingSource baseSource;
	protected long startPos;
	protected long endPos;
	
	protected Token(T type, long startPos, long endPos, ParsingSource baseSource) {
		this.type = type;
		this.value = null;
		this.baseSource = baseSource;
		this.startPos = startPos;
		this.endPos = endPos;
	}

	public T getType() {
		return this.type;
	}
	public String getValue() {
		if (this.value == null) {
			this.value = this.baseSource.substring(this.startPos, this.endPos);
		}
		return this.value;
	}
	public long getStartPos() {
		return this.startPos;
	}
	public long getEndPos() {
		return this.endPos;
	}
}

class SimpleToken extends Token<ParsingRule> {
	protected SimpleToken(ParsingRule type, long startPos, long endPos, ParsingSource baseSource) {
		super(type, startPos, endPos, baseSource);
	}
	
	static SimpleToken create(ParsingRule type, long startPos, long endPos, ParsingSource baseSource) {
		if (type.localName.equals(type.localName.toUpperCase())) {
			return new SimpleTokenComparedByValue(type, startPos, endPos, baseSource);
		}
		else {
			return new SimpleTokenComparedByType(type, startPos, endPos, baseSource);
		}
	}
}
class SimpleTokenComparedByValue extends SimpleToken {
	SimpleTokenComparedByValue(ParsingRule type, long startPos, long endPos, ParsingSource baseSource) {
		super(type, startPos, endPos, baseSource);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		else if (!(obj instanceof SimpleTokenComparedByValue)) {
			return false;
		}
		else {
			SimpleTokenComparedByValue token = (SimpleTokenComparedByValue)obj;
			return this.getValue().equals(token.getValue());
		}
	}

	@Override
	public int hashCode() {
		return this.getValue().hashCode();
	}
	
	@Override
	public String toString() {
		return this.getValue();
	}
}
class SimpleTokenComparedByType extends SimpleToken {
	SimpleTokenComparedByType(ParsingRule type, long startPos, long endPos, ParsingSource baseSource) {
		super(type, startPos, endPos, baseSource);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		else if (!(obj instanceof SimpleTokenComparedByType)) {
			return false;
		}
		else {
			SimpleTokenComparedByType token = (SimpleTokenComparedByType)obj;
			return this.getType() == token.getType();
		}
	}

	@Override
	public int hashCode() {
		return this.getType().hashCode();
	}
	
	@Override
	public String toString() {
		return this.type.localName;
	}
}

class MetaToken extends Token<String> {
	MetaToken(String type, long startPos, long endPos, ParsingSource baseSource) {
		super(type, startPos, endPos, baseSource);
	}
	
	@Override
	public String toString() {
		return this.type;
	}
	
	public String getLeftDelimiter() {
		return String.valueOf(this.type.charAt(0));
	}
	public String getRightDelimiter() {
		return String.valueOf(this.type.charAt(2));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		else if (!(obj instanceof MetaToken)) {
			return false;
		}
		else {
			MetaToken token = (MetaToken)obj;
			return this.getType().equals(token.getType());
		}
	}

	@Override
	public int hashCode() {
		return this.getType().hashCode();
	}
}