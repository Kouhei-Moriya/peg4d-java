package org.peg4d;

import org.peg4d.ParsingContextMemo.ObjectMemo;
import org.peg4d.vm.CodeGenerator2;

public abstract class PExpression {
	public final static int CyclicRule       = 1;
	public final static int HasNonTerminal    = 1 << 1;
	public final static int HasString         = 1 << 2;
	public final static int HasCharacter      = 1 << 3;
	public final static int HasAny            = 1 << 4;
	public final static int HasRepetition     = 1 << 5;
	public final static int HasOptional       = 1 << 6;
	public final static int HasChoice         = 1 << 7;
	public final static int HasAnd            = 1 << 8;
	public final static int HasNot            = 1 << 9;
	
	public final static int HasConstructor    = 1 << 10;
	public final static int HasConnector      = 1 << 11;
	public final static int HasTagging        = 1 << 12;
	public final static int HasMessage        = 1 << 13;
	public final static int HasContext        = 1 << 14;
	public final static int HasReserved       = 1 << 15;
	public final static int hasReserved2       = 1 << 16;
	public final static int Mask = HasNonTerminal | HasString | HasCharacter | HasAny
	                             | HasRepetition | HasOptional | HasChoice | HasAnd | HasNot
	                             | HasConstructor | HasConnector | HasTagging | HasMessage 
	                             | HasReserved | hasReserved2 | HasContext;
	public final static int HasLazyNonTerminal = Mask;

	public final static int LeftObjectOperation    = 1 << 17;
	public final static int PossibleDifferentRight = 1 << 18;
	
	public final static int NoMemo            = 1 << 20;
	public final static int Debug             = 1 << 24;
	
	public final static int ParsedResult      = -1;
	public final static int SourcePosition          = 0;
	public final static int FailurePosition          = 1;
	
	
	int        flag       = 0;
	int        uniqueId   = 0;
	int        consumed   = 0;
		
	protected PExpression(int flag) {
		this.flag = flag;
	}

	abstract PExpression dup();
	
	protected abstract void visit(ParsingExpressionVisitor visitor);
	public PExpression getExpression() {
		return this;
	}
	public abstract void simpleMatch(ParsingContext context);
	final void writeCode(CodeGenerator2 gen){}

	boolean checkFirstByte(int ch) {
		return true;
	}

	public final boolean is(int uflag) {
		return ((this.flag & uflag) == uflag);
	}

	public void set(int uflag) {
		this.flag = this.flag | uflag;
	}

	protected void derived(PExpression e) {
		this.flag |= (e.flag & PExpression.Mask);
	}
	
	public final boolean isUnique() {
		return this.uniqueId > 0;
	}
	
	public int size() {
		return 0;
	}
	public PExpression get(int index) {
		return this;  // to avoid NullPointerException
	}
	
	public PExpression get(int index, PExpression def) {
		return def;
	}

	private final static GrammarFormatter DefaultFormatter = new GrammarFormatter();
	
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		DefaultFormatter.format(this, sb);
		return sb.toString();
	}

	public final String format(String name, GrammarFormatter fmt) {
		StringBuilder sb = new StringBuilder();
		fmt.formatRule(name, this, sb);
		return sb.toString();
	}
	public final String format(String name) {
		return this.format(name, new GrammarFormatter());
	}
	protected void warning(String msg) {
		if(Main.VerbosePeg && Main.StatLevel == 0) {
			Main._PrintLine("PEG warning: " + msg);
		}
	}
	public final boolean hasObjectOperation() {
		return this.is(PExpression.HasConstructor) 
				|| this.is(PExpression.HasConnector) 
				|| this.is(PExpression.HasTagging) 
				|| this.is(PExpression.HasMessage);
	}
	
	// factory
	
	private static UMap<PExpression> uniqueExpressionMap = new UMap<PExpression>();
	private static int unique = 0;
	
	private static boolean StringSpecialization = true;
	private static boolean CharacterChoice      = true;

//	private static String prefixNonTerminal = "a\b";
	private static String prefixString = "t\b";
	private static String prefixByte   = "b\b";
	private static String prefixCharacter = "c\b";
	private static String prefixNot = "!\b";
	private static String prefixAnd = "&\b";
	private static String prefixZeroMore = "*\b";
	private static String prefixOptional = "?\b";
	private static String prefixSequence = " \b";
	private static String prefixChoice = "/\b";
	
	private static String prefixSetter  = "S\b";
	private static String prefixTagging = "T\b";
	private static String prefixMessage = "M\b";
	
	public final static int newExpressionId() {
		unique++;
		return unique;
	}

	private static PExpression getUnique(String t) {
		PExpression e = uniqueExpressionMap.get(t);
		if(e != null) {
			Main.printVerbose("unique", "" + e);
			return e;
		}
		return null;
	}

	private static PExpression putUnique(String t, PExpression e) {
//		if(Main.AllExpressionMemo && !e.is(PExpression.NoMemo)) {
//			e.base.EnabledMemo += 1;
//			e = newMemo(e);
//		}
		uniqueExpressionMap.put(t, e);
		e.uniqueId = newExpressionId();
		return e;
	}

//	public final PExpression newMemo(PExpression e) {
//		if(e instanceof ParsingMemo) {
//			return e;
//		}
//		return new ParsingMemo(e);
//	}

	public static final PExpression newString(String text) {
		String key = prefixString + text;
		PExpression e = getUnique(key);
		if(e == null) {
			e = putUnique(key, newStringImpl(text));
		}
		return e;
	}

	private static PExpression newStringImpl(String text) {
		byte[] utf8 = ParsingCharset.toUtf8(text);
		if(StringSpecialization) {
			if(utf8.length == 1) {
				return new PByteChar(0, utf8[0]);
			}
			if(text.length() == 1) {
				int c = text.charAt(0);
				return newCharacter(new UnicodeRange(c,c));
			}
		}
		return new PString(0, text, utf8);	
	}

	public final static PExpression newByteChar(int ch) {
		String key = prefixByte + (ch & 0xff);
		PExpression e = getUnique(key);
		if(e == null) {
			e = putUnique(key, new PByteChar(0, ch));
		}
		return e;
	}

	public final static PExpression newAny(String text) {
		PExpression e = getUnique(text);
		if(e == null) {
			e = new PAny(0);
			e = putUnique(text, e);
		}
		return e;
	}
	
	public final static PExpression newCharacter(ParsingCharset u) {
		String key = prefixCharacter + u.key();
		//System.out.println(u.toString() + ": " + u.key());
		PExpression e = getUnique(key);
		if(e == null) {
			e = putUnique(key, new PCharacter(0, u));
		}
		return e;
	}

	private final static String pkey(PExpression p) {
		return "#" + p.uniqueId;
	}
	
	public final static PExpression newOptional(PExpression p) {
		if(p.isUnique()) {
			String key = prefixOptional + pkey(p);
			PExpression e = getUnique(key);
			if(e == null) {
				e = putUnique(key, newOptionalImpl(p));
			}
			return e;
		}
		return newOptionalImpl(p);
	}

	private static PExpression newOptionalImpl(PExpression p) {
		if(StringSpecialization) {
			if(p instanceof PByteChar) {
				return new POptionalByteChar(0, (PByteChar)p);
			}
			if(p instanceof PString) {
				return new POptionalString(0, (PString)p);
			}
			if(p instanceof PCharacter) {
				return new POptionalCharacter(0, (PCharacter)p);
			}
		}
//		if(p instanceof PSequence) {
//			
//		}
		return new POptional(0, p);
	}
	
	public final static PExpression newMatch(PExpression p) {
		if(!p.hasObjectOperation() && !p.is(PExpression.HasNonTerminal)) {
			return p;
		}
		return new ParsingMatch(p);
	}
	
	public final static PExpression newOneMore(PExpression p) {
		UList<PExpression> l = new UList<PExpression>(new PExpression[2]);
		l.add(p);
		l.add(newRepetition(p));
		return newSequence(l);
	}
	
	public final static PExpression newRepetition(PExpression p) {
		if(p.isUnique()) {
			String key = prefixZeroMore + pkey(p);
			PExpression e = getUnique(key);
			if(e == null) {
				e = putUnique(key, newRepetitionImpl(p));
			}
			return e;
		}
		return newRepetitionImpl(p);
	}

	private static PExpression newRepetitionImpl(PExpression p) {
		if(p instanceof PCharacter) {
			return new PZeroMoreCharacter(0, (PCharacter)p);
		}
		return new PRepetition(0, p);
	}

	public final static PExpression newTimes(int ntimes, PExpression p) {
		if(ntimes == 1) {
			return p;
		}
		UList<PExpression> l = new UList<PExpression>(new PExpression[ntimes]);
		for(int i = 0; i < ntimes; i++) {
			addSequence(l, p);
		}
		return newSequence(l);
	}

	public final static PExpression newAnd(PExpression p) {
		if(p.isUnique()) {
			String key = prefixAnd + pkey(p);
			PExpression e = getUnique(key);
			if(e == null) {
				e = putUnique(key, newAndImpl(p));
			}
			return e;
		}
		return newAndImpl(p);
	}
	
	private static PExpression newAndImpl(PExpression p) {
		return new PAnd(0, p);
	}

	public final static PExpression newNot(PExpression p) {
		if(p.isUnique()) {
			String key = prefixNot + pkey(p);
			PExpression e = getUnique(key);
			if(e == null) {
				e = putUnique(key, newNotImpl(p));
			}
			return e;
		}
		return newNotImpl(p);
	}
	
	private static PExpression newNotImpl(PExpression p) {
		if(StringSpecialization) {
			if(p instanceof PString) {
				if(p instanceof PByteChar) {
					return new PNotByteChar(0, (PByteChar)p);
				}
				return new PNotString(0, (PString)p);
			}
			if(p instanceof PCharacter) {
				return new PNotCharacter(0, (PCharacter)p);
			}
		}
		if(p instanceof ParsingOperation) {
			p = ((ParsingOperation)p).inner;
		}
		return new PNot(0, newMatch(p));
	}
	
	private static boolean isUnique(UList<PExpression> l) {
		for(int i = 0; i < l.size(); i++) {
			PExpression se = l.ArrayValues[i];
			if(!se.isUnique()) {
				return false;
			}
		}
		return true;
	}
	public final static PExpression newChoice(UList<PExpression> l) {
		if(l.size() == 1) {
			return l.ArrayValues[0];
		}
		if(isUnique(l)) {
			String key = prefixChoice;
			boolean isAllText = true;
			for(int i = 0; i < l.size(); i++) {
				PExpression p = l.ArrayValues[i];
				key += pkey(p);
				if(!(p instanceof PString) && !(p instanceof PString)) {
					isAllText = false;
				}
			}
			PExpression e = getUnique(key);
			if(e == null) {
				e = putUnique(key, newChoiceImpl(l, isAllText));
			}
			return e;
		}
		return newChoiceImpl(l, false);
	}

	private static PExpression newChoiceImpl(UList<PExpression> l, boolean isAllText) {
//		if(peg.optimizationLevel > 0) {
////			if(isAllText) {
////				return new PegWordChoice(0, l);
////			}
//		}
//		if(peg.optimizationLevel > 2) {
//			return new PMappedChoice(0, l);
//		}		
		return new PChoice(0, l);
	}
	
	public final static PExpression mergeChoice(PExpression e, PExpression e2) {
		if(e == null) return e2;
		if(e2 == null) return e;
		UList<PExpression> l = new UList<PExpression>(new PExpression[e.size()+e2.size()]);
		addChoice(l, e);
		addChoice(l, e2);
		return newChoice(l);
	}
	
	public final static void addChoice(UList<PExpression> l, PExpression e) {
		if(e instanceof PChoice) {
			for(int i = 0; i < e.size(); i++) {
				addChoice(l, e.get(i));
			}
			return;
		}
		if(CharacterChoice && l.size() > 0 && (e instanceof PByteChar || e instanceof PCharacter)) {
			PExpression pe = l.ArrayValues[l.size()-1];
			if(pe instanceof PByteChar || pe instanceof PCharacter) {
				ParsingCharset b = (e instanceof PByteChar)
					? new ByteCharset(((PByteChar) e).byteChar, ((PByteChar) e).byteChar)
					: ((PCharacter)e).charset.dup();
				b = mergeUpdate(b, pe);
				l.ArrayValues[l.size()-1] = newCharacter(b);
				return;
			}
		}
		l.add(e);
	}
	
	private static final ParsingCharset mergeUpdate(ParsingCharset cu, PExpression e) {
		if(e instanceof PByteChar) {
			return cu.appendByte(((PByteChar) e).byteChar, ((PByteChar) e).byteChar);
		}
		ParsingCharset u = ((PCharacter) e).charset;
		if(u instanceof UnicodeRange) {
			UnicodeRange r = (UnicodeRange)u;
			return cu.appendChar(r.beginChar, r.endChar);
		}
		ByteCharset ub = (ByteCharset)u;
		for(int i = 0; i < ub.bitMap.length; i++) {
			if(ub.bitMap[i]) {
				cu = cu.appendByte(i, i);
			}
		}
		if(ub.unicodeRangeList != null) {
			for(int i = 0; i < ub.unicodeRangeList.size(); i++) {
				UnicodeRange r = ub.unicodeRangeList.ArrayValues[i];
				cu = cu.appendChar(r.beginChar, r.endChar);
			}
		}
		return cu;
	}
	
	public final static PExpression newSequence(UList<PExpression> l) {
		if(l.size() == 1) {
			return l.ArrayValues[0];
		}
		if(l.size() == 0) {
			return newString("");
		}
		if(isUnique(l)) {
			String key = prefixSequence;
			for(int i = 0; i < l.size(); i++) {
				key += pkey(l.ArrayValues[i]);
			}
			PExpression e = getUnique(key);
			if(e == null) {
				e = putUnique(key, newSequenceImpl(l));
			}
			return e;
		}
		return newSequenceImpl(l);
	}
	
	public final static void addSequence(UList<PExpression> l, PExpression e) {
		if(e instanceof PSequence) {
			for(int i = 0; i < e.size(); i++) {
				addSequence(l, e.get(i));
			}
		}
		else {
			l.add(e);
		}
	}

	private static PExpression newSequenceImpl(UList<PExpression> l) {
		PExpression orig = new PSequence(0, l);
//		if(peg.optimizationLevel > 1) {
//			int nsize = l.size()-1;
//			if(nsize > 0 && l.ArrayValues[nsize] instanceof PAny) {
//				boolean allNot = true;
//				for(int i = 0; i < nsize; i++) {
//					if(!(l.ArrayValues[nsize] instanceof PNot)) {
//						allNot = false;
//						break;
//					}
//				}
//				if(allNot) {
//					return newNotAnyImpl(orig, l, nsize);
//				}
//			}
//		}
		return orig;
	}

	public final static PExpression newConstructor(PExpression p) {
		PExpression e = new PConstructor(0, false, toSequenceList(p));
//		if(peg.memoFactor != 0 && (Main.AllExpressionMemo || Main.ObjectFocusedMemo)) {
//			e = new ParsingMemo(e);
//		}
		return e;
	}

	public final static PExpression newJoinConstructor(PExpression p) {
		PExpression e = new PConstructor(0, true, toSequenceList(p));
//		if(peg.memoFactor != 0 && (Main.AllExpressionMemo || Main.ObjectFocusedMemo)) {
//			e = new ParsingMemo(e);
//		}
		return e;
	}
	
	public final static UList<PExpression> toSequenceList(PExpression e) {
		if(e instanceof PSequence) {
			return ((PSequence) e).list;
		}
		UList<PExpression> l = new UList<PExpression>(new PExpression[1]);
		l.add(e);
		return l;
	}
	
	private static PExpression newNotAnyImpl(PExpression orig, UList<PExpression> l, int nsize) {
//		if(nsize == 1) {
//			return new PNotAny(0, (PNot)l.ArrayValues[0], orig);
//		}
		return orig;
	}
	
	public final static PExpression newConnector(PExpression p, int index) {
		if(p.isUnique()) {
			String key = prefixSetter + index + pkey(p);
			PExpression e = getUnique(key);
			if(e == null) {
				e = putUnique(key, new PConnector(0, p, index));
			}
			return e;
		}
		return new PConnector(0, p, index);
	}

	public final static PExpression newTagging(ParsingTag tag) {
		String key = prefixTagging + tag.key();
		PExpression e = getUnique(key);
		if(e == null) {
			e = putUnique(key, new PTagging(0, tag));
		}
		return e;
	}

	public final static PExpression newMessage(String msg) {
		String key = prefixMessage + msg;
		PExpression e = getUnique(key);
		if(e == null) {
			e = putUnique(key, new PMessage(0, msg));
		}
		return e;
	}
	
	public final static PExpression newDebug(PExpression e) {
		return new ParsingDebug(e);
	}

	public final static PExpression newFail(String message) {
		return new ParsingFail(0, message);
	}

	private static PExpression catchExpression = null;

	public final static PExpression newCatch() {
		if(catchExpression == null) {
			catchExpression = new ParsingCatch(0);
			catchExpression.uniqueId = PExpression.newExpressionId();
		}
		return catchExpression;
	}
	
	public final static PExpression newFlag(String flagName) {
		return new ParsingIfFlag(0, flagName);
	}

	public final static PExpression newEnableFlag(String flagName, PExpression e) {
		return new ParsingWithFlag(flagName, e);
	}

	public final static PExpression newDisableFlag(String flagName, PExpression e) {
		return new ParsingWithoutFlag(flagName, e);
	}

	private static PExpression indentExpression = null;

	public final static PExpression newIndent(PExpression e) {
		if(e == null) {
			if(indentExpression == null) {
				indentExpression = new ParsingIndent(0);
				indentExpression.uniqueId = PExpression.newExpressionId();
			}
			return indentExpression;
		}
		return new ParsingStackIndent(e);
	}

	
}

class PNonTerminal extends PExpression {
	Grammar base;
	String symbol;
	PExpression    resolvedExpression = null;
	PNonTerminal(Grammar base, int flag, String ruleName) {
		super(flag | PExpression.HasNonTerminal | PExpression.NoMemo);
		this.base = base;
		this.symbol = ruleName;
	}
	@Override
	PExpression dup() {
		return new PNonTerminal(base, flag, symbol);
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitNonTerminal(this);
	}
	@Override boolean checkFirstByte(int ch) {
		if(this.resolvedExpression != null) {
			return this.resolvedExpression.checkFirstByte(ch);
		}
		return true;
	}
	final PExpression getNext() {
		if(this.resolvedExpression == null) {
			return this.base.getExpression(this.symbol);
		}
		return this.resolvedExpression;
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		this.resolvedExpression.simpleMatch(context);
	}
}

abstract class PTerminal extends PExpression {
	PTerminal (int flag) {
		super(flag);
	}
	@Override
	public final int size() {
		return 0;
	}
	@Override
	public final PExpression get(int index) {
		return this;  // just avoid NullPointerException
	}
}

class PString extends PTerminal {
	String text;
	byte[] utf8;
	PString(int flag, String text, byte[] utf8) {
		super(PExpression.HasString | PExpression.NoMemo | flag);
		this.text = text;
		this.utf8 = utf8;
	}
	PString(int flag, String text) {
		this(flag, text, ParsingCharset.toUtf8(text));
	}
	PString(int flag, int ch) {
		super(PExpression.HasString | PExpression.NoMemo | flag);
		utf8 = new byte[1];
		utf8[0] = (byte)ch;
		if(ch >= ' ' && ch < 127) {
			this.text = String.valueOf((char)ch);
		}
		else {
			this.text = String.format("0x%x", ch);
		}
	}
	@Override
	PExpression dup() { 
		return new PString(flag, text, utf8); 
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitString(this);
	}
	@Override boolean checkFirstByte(int ch) {
		if(this.text.length() == 0) {
			return true;
		}
		return ParsingCharset.getFirstChar(this.utf8) == ch;
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchText(this.utf8);
	}
}

class PByteChar extends PString {
	int byteChar;
	PByteChar(int flag, int ch) {
		super(flag, ch);
		this.byteChar = this.utf8[0] & 0xff;
	}
	@Override PExpression dup() { 
		return new PByteChar(flag, byteChar);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchByteChar(this.byteChar);
	}
}

class PAny extends PTerminal {
	PAny(int flag) {
		super(PExpression.HasAny | PExpression.NoMemo | flag);
	}
	@Override PExpression dup() { return this; }
	@Override boolean checkFirstByte(int ch) {
		return true;
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitAny(this);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchAnyChar();
	}
}

class PCharacter extends PTerminal {
	ParsingCharset charset;
	PCharacter(int flag, ParsingCharset charset) {
		super(PExpression.HasCharacter | PExpression.NoMemo | flag);
		this.charset = charset;
	}
	@Override PExpression dup() { return this; }
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitCharacter(this);
	}
	@Override boolean checkFirstByte(int ch) {
		return this.charset.hasByte(ch);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchCharset(this.charset);
	}
}


//class PNotAny extends PTerm {
//	PNot not;
//	PExpression exclude;
//	PExpression orig;
//	public PNotAny(Grammar base, int flag, PNot e, PExpression orig) {
//		super(base, flag | PExpression.NoMemo);
//		this.not = e;
//		this.exclude = e.inner;
//		this.orig = orig;
//	}
//	@Override
//	protected void visit(ParsingVisitor visitor) {
//		visitor.visitNotAny(this);
//	}
//	@Override boolean checkFirstByte(int ch) {
//		return this.not.checkFirstByte(ch) && this.orig.checkFirstByte(ch);
//	}
//	@Override
//	public ParsingObject simpleMatch(ParsingObject left, ParsingContext2 context) {
//		long pos = context.getPosition();
//		ParsingObject right = this.exclude.simpleMatch(left, context);
//		if(context.isFailure()) {
//			assert(pos == context.getPosition());
//			if(context.hasByteChar()) {
//				context.consume(1);
//				return left;
//			}
//		}
//		else {
//			context.rollback(pos);
//		}
//		return context.foundFailure(this);
//	}
//}


abstract class PUnary extends PExpression {
	PExpression inner;
	PUnary(int flag, PExpression e) {
		super(flag);
		this.inner = e;
		this.derived(e);
	}
	@Override
	public final int size() {
		return 1;
	}
	@Override
	public final PExpression get(int index) {
		return this.inner;
	}
}

class POptional extends PUnary {
	POptional(int flag, PExpression e) {
		super(flag | PExpression.HasOptional | PExpression.NoMemo, e);
	}
	@Override PExpression dup() { 
		return new POptional(flag, inner); 
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitOptional(this);
	}
	@Override boolean checkFirstByte(int ch) {
		return true;
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		long f = context.rememberFailure();
		ParsingObject left = context.left;
//		context.opRememberFailurePosition();
//		context.opStoreObject();
		this.inner.simpleMatch(context);
		if(context.isFailure()) {
			context.left = left;
			context.forgetFailure(f);
		}
	}

}

class POptionalString extends POptional {
	byte[] utf8;
	POptionalString(int flag, PString e) {
		super(flag | PExpression.NoMemo, e);
		this.utf8 = e.utf8;
	}
	@Override PExpression dup() { 
		return new POptionalString(flag, (PString)inner); 
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchOptionalText(this.utf8);
	}
}

class POptionalByteChar extends POptional {
	int byteChar;
	POptionalByteChar(int flag, PByteChar e) {
		super(flag | PExpression.NoMemo, e);
		this.byteChar = e.byteChar;
	}
	@Override PExpression dup() { 
		return new POptionalByteChar(flag, (PByteChar)inner); 
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchOptionalByteChar(this.byteChar);
	}
}

class POptionalCharacter extends POptional {
	ParsingCharset charset;
	POptionalCharacter(int flag, PCharacter e) {
		super(flag | PExpression.NoMemo, e);
		this.charset = e.charset;
	}
	@Override PExpression dup() { 
		return new POptionalCharacter(flag, (PCharacter)inner); 
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchOptionalCharset(this.charset);
	}
}

class PRepetition extends PUnary {
//	public int atleast = 0; 
	PRepetition(int flag, PExpression e/*, int atLeast*/) {
		super(flag | PExpression.HasRepetition, e);
//		this.atleast = atLeast;
	}
	@Override PExpression dup() { 
		return new PRepetition(flag, inner/*, atleast*/); 
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitRepetition(this);
	}
	@Override boolean checkFirstByte(int ch) {
//		if(this.atleast > 0) {
//			return this.inner.checkFirstByte(ch);
//		}
		return true;
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		long ppos = -1;
		long pos = context.getPosition();
		long f = context.rememberFailure();
		while(ppos < pos) {
			ParsingObject left = context.left;
			this.inner.simpleMatch(context);
			if(context.isFailure()) {
				context.left = left;
				break;
			}
			ppos = pos;
			pos = context.getPosition();
		}
		context.forgetFailure(f);
	}
}

class PZeroMoreCharacter extends PRepetition {
	ParsingCharset charset;
	PZeroMoreCharacter(int flag, PCharacter e) {
		super(flag, e);
		this.charset = e.charset;
	}
	@Override PExpression dup() { 
		return new PZeroMoreCharacter(flag, (PCharacter)inner); 
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		long pos = context.getPosition();
		int consumed = 0;
		do {
			consumed = this.charset.consume(context.source, pos);
			pos += consumed;
		}
		while(consumed > 0);
		context.setPosition(pos);
	}
}

class PAnd extends PUnary {
	PAnd(int flag, PExpression e) {
		super(flag | PExpression.HasAnd, e);
	}
	@Override PExpression dup() { 
		return new PAnd(flag, inner); 
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitAnd(this);
	}
	@Override
	boolean checkFirstByte(int ch) {
		return this.inner.checkFirstByte(ch);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		long pos = context.getPosition();
		this.inner.simpleMatch(context);
		context.rollback(pos);
	}
}

class PNot extends PUnary {
	PNot(int flag, PExpression e) {
		super(PExpression.HasNot | flag, e);
	}
	@Override PExpression dup() { 
		return new PNot(flag, inner); 
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitNot(this);
	}
	@Override
	boolean checkFirstByte(int ch) {
		return !this.inner.checkFirstByte(ch);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		long pos = context.getPosition();
		long f   = context.rememberFailure();
		ParsingObject left = context.left;
		this.inner.simpleMatch(context);
		if(context.isFailure()) {
			context.forgetFailure(f);
			context.left = left;
		}
		else {
			context.opFailure(this);
		}
		context.rollback(pos);
	}
}

class PNotString extends PNot {
	byte[] utf8;
	PNotString(int flag, PString e) {
		super(flag | PExpression.NoMemo, e);
		this.utf8 = e.utf8;
	}
	@Override PExpression dup() { 
		return new PNotString(flag, (PString)inner); 
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchTextNot(utf8);
	}
}

class PNotByteChar extends PNotString {
	int byteChar;
	PNotByteChar(int flag, PByteChar e) {
		super(flag, e);
		this.byteChar = e.byteChar;
	}
	@Override PExpression dup() { 
		return new PNotByteChar(flag, (PByteChar)inner); 
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchByteCharNot(this.byteChar);
	}
}	

class PNotCharacter extends PNot {
	ParsingCharset charset;
	PNotCharacter(int flag, PCharacter e) {
		super(flag | PExpression.NoMemo, e);
		this.charset = e.charset;
	}
	@Override PExpression dup() { 
		return new PNotCharacter(flag, (PCharacter)inner); 
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opMatchCharsetNot(this.charset);
	}
}

abstract class PList extends PExpression {
	UList<PExpression> list;
	int length = 0;
	PList(int flag, UList<PExpression> list) {
		super(flag);
		this.list = list;
	}
	@Override
	public final int size() {
		return this.list.size();
	}
	@Override
	public final PExpression get(int index) {
		return this.list.ArrayValues[index];
	}
	public final void set(int index, PExpression e) {
		this.list.ArrayValues[index] = e;
	}
	
	@Override
	public final PExpression get(int index, PExpression def) {
		if(index < this.size()) {
			return this.list.ArrayValues[index];
		}
		return def;
	}

	private boolean isOptional(PExpression e) {
		if(e instanceof POptional) {
			return true;
		}
		if(e instanceof PRepetition) {
			return true;
		}
		return false;
	}

	private boolean isUnconsumed(PExpression e) {
		if(e instanceof PNot && e instanceof PAnd) {
			return true;
		}
		if(e instanceof PString && ((PString)e).utf8.length == 0) {
			return true;
		}
		if(e instanceof ParsingIndent) {
			return true;
		}
		return false;
	}
		
	@Override
	boolean checkFirstByte(int ch) {
		for(int start = 0; start < this.size(); start++) {
			PExpression e = this.get(start);
			if(e instanceof PTagging || e instanceof PMessage) {
				continue;
			}
			if(this.isOptional(e)) {
				if(((PUnary)e).inner.checkFirstByte(ch)) {
					return true;
				}
				continue;  // unconsumed
			}
			if(this.isUnconsumed(e)) {
				if(!e.checkFirstByte(ch)) {
					return false;
				}
				continue;
			}
			return e.checkFirstByte(ch);
		}
		return true;
	}

	public final PExpression trim() {
		int size = this.size();
		boolean hasNull = true;
		while(hasNull) {
			hasNull = false;
			for(int i = 0; i < size-1; i++) {
				if(this.get(i) == null && this.get(i+1) != null) {
					this.swap(i,i+1);
					hasNull = true;
				}
			}
		}
		for(int i = 0; i < this.size(); i++) {
			if(this.get(i) == null) {
				size = i;
				break;
			}
		}
		if(size == 0) {
			return null;
		}
		if(size == 1) {
			return this.get(0);
		}
		this.list.clear(size);
		return this;
	}
	
	public final void swap(int i, int j) {
		PExpression e = this.list.ArrayValues[i];
		this.list.ArrayValues[i] = this.list.ArrayValues[j];
		this.list.ArrayValues[j] = e;
	}
}

class PSequence extends PList {
	PSequence(int flag, UList<PExpression> l) {
		super(flag, l);
	}
	@Override
	PExpression dup() {
		return new PSequence(flag, list);
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitSequence(this);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		long pos = context.getPosition();
		int mark = context.markObjectStack();
		for(int i = 0; i < this.size(); i++) {
			this.get(i).simpleMatch(context);
			//System.out.println("Attmpt Sequence " + this.get(i) + " isFailure: " + context.isFailure());
			if(context.isFailure()) {
				context.abortLinkLog(mark);
				context.rollback(pos);
				break;
			}
		}
	}

}

class PChoice extends PList {
	PChoice(int flag, UList<PExpression> list) {
		super(flag | PExpression.HasChoice, list);
	}
	@Override
	PExpression dup() {
		return new PChoice(flag, list);
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitChoice(this);
	}
	@Override
	boolean checkFirstByte(int ch) {
		for(int i = 0; i < this.size(); i++) {
			if(this.get(i).checkFirstByte(ch)) {
				return true;
			}
		}
		return false;
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		long f = context.rememberFailure();
		ParsingObject left = context.left;
		for(int i = 0; i < this.size(); i++) {
			context.left = left;
			this.get(i).simpleMatch(context);
			//System.out.println("[" + i+ "]: isFailure?: " + context.isFailure() + " e=" + this.get(i));
			if(!context.isFailure()) {
				context.forgetFailure(f);
				return;
			}
		}
		assert(context.isFailure());
	}

}

class PMappedChoice extends PChoice {
	PExpression[] caseOf = null;
	PMappedChoice(int flag, UList<PExpression> list) {
		super(flag, list);
	}
	@Override
	PExpression dup() {
		return new PMappedChoice(flag, list);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		int ch = context.getByteChar();
		if(this.caseOf == null) {
			tryPrediction();
		}
		caseOf[ch].simpleMatch(context);
	}
	void tryPrediction() {
		if(this.caseOf == null) {
			this.caseOf = new PExpression[ParsingCharset.MAX];
			PExpression failed = new PAlwaysFailure(this);
			for(int ch = 0; ch < ParsingCharset.MAX; ch++) {
				this.caseOf[ch] = selectC1(ch, failed);
			}
//			this.base.PredictionOptimization += 1;
		}
	}
	private PExpression selectC1(int ch, PExpression failed) {
		PExpression e = null;
		UList<PExpression> l = null; // new UList<Peg>(new Peg[2]);
		for(int i = 0; i < this.size(); i++) {
			if(this.get(i).checkFirstByte(ch)) {
				if(e == null) {
					e = this.get(i);
				}
				else {
					if(l == null) {
						l = new UList<PExpression>(new PExpression[2]);
						l.add(e);
					}
					l.add(get(i));
				}
			}
		}
		if(l != null) {
			e = new PChoice(0, l);
//			e.base.UnpredicatedChoiceL1 += 1;
		}
		else {
			if(e == null) {
				e = failed;
			}
//			e.base.PredicatedChoiceL1 +=1;
		}
		return e;
	}
}

//class PegWordChoice extends PChoice {
//	ParsingCharset charset = null;
//	UList<byte[]> wordList = null;
//	PegWordChoice(Grammar base, int flag, UList<PExpression> list) {
//		super(base, flag | PExpression.HasChoice, list);
//		this.wordList = new UList<byte[]>(new byte[list.size()][]);
//		for(int i = 0; i < list.size(); i++) {
//			PExpression se = list.ArrayValues[i];
//			if(se instanceof PString1) {
//				if(charset == null) {
//					charset = new ParsingCharset("");
//				}
//				charset.append(((PString1)se).symbol1);
//			}
//			if(se instanceof PCharacter) {
//				if(charset == null) {
//					charset = new ParsingCharset("");
//				}
//				charset.append(((PCharacter)se).charset);
//			}
//			if(se instanceof PString) {
//				wordList.add(((PString)se).utf8);
//			}
//		}
//	}
//	
//	@Override
//	public ParsingObject simpleMatch(ParsingObject left, ParserContext context) {
//		if(this.charset != null) {
//			if(context.match(this.charset)) {
//				return left;
//			}
//		}
//		for(int i = 0; i < this.wordList.size(); i++) {
//			if(context.match(this.wordList.ArrayValues[i])) {
//				return left;
//			}
//		}
//		return context.foundFailure(this);
//	}
//}

class PAlwaysFailure extends PString {
	PExpression dead;
	PAlwaysFailure(PExpression dead) {
		super(0, "\0");
		this.dead = dead;
	}
	@Override
	PExpression dup() {
		return new PAlwaysFailure(dead);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opFailure(dead);
	}
}

class PConnector extends PUnary {
	public int index;
	PConnector(int flag, PExpression e, int index) {
		super(flag | PExpression.HasConnector | PExpression.NoMemo, e);
		this.index = index;
	}
	@Override
	PExpression dup() {
		return new PConnector(flag, inner, index);
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitConnector(this);
	}
	@Override
	boolean checkFirstByte(int ch) {
		return this.inner.checkFirstByte(ch);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		ParsingObject left = context.left;
		assert(left != null);
		long pos = left.getSourcePosition();
		//System.out.println("== DEBUG connecting .. " + this.inner);
		this.inner.simpleMatch(context);
		//System.out.println("== DEBUG same? " + (context.left == left) + " by " + this.inner);
		if(context.isFailure() || context.left == left) {
			return;
		}
		if(context.canTransCapture()) {
			context.logLink(left, this.index, context.left);
		}
		else {
			left.setSourcePosition(pos);
		}
		context.left = left;
	}
}

class PTagging extends PTerminal {
	ParsingTag tag;
	PTagging(int flag, ParsingTag tag) {
		super(PExpression.HasTagging | PExpression.NoMemo | flag);
		this.tag = tag;
	}
	@Override
	PExpression dup() {
		return new PTagging(flag, tag);
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitTagging(this);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		if(context.canTransCapture()) {
			context.left.setTag(this.tag);
		}
	}
}

class PMessage extends PTerminal {
	String symbol;
	PMessage(int flag, String message) {
		super(flag | PExpression.NoMemo | PExpression.HasMessage);
		this.symbol = message;
	}
	@Override
	PExpression dup() {
		return new PMessage(flag, symbol);
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitMessage(this);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		if(context.canTransCapture()) {
			context.left.setValue(this.symbol);
		}
	}
}

class PConstructor extends PList {
	boolean leftJoin = false;
	int prefetchIndex = 0;
	PConstructor(int flag, boolean leftJoin, UList<PExpression> list) {
		super(flag | PExpression.HasConstructor, list);
		this.leftJoin = leftJoin;
	}
	@Override
	PExpression dup() {
		return new PConstructor(flag, leftJoin, list);
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitConstructor(this);
	}
	
	@Override
	public void simpleMatch(ParsingContext context) {
		long startIndex = context.getPosition();
		ParsingObject left = context.left;
		if(context.isRecognitionMode()) {
			ParsingObject newone = context.newParsingObject(startIndex, this);
			context.left = newone;
			for(int i = 0; i < this.size(); i++) {
				this.get(i).simpleMatch(context);
				if(context.isFailure()) {
					context.rollback(startIndex);
					return;
				}
			}
			context.left = newone;
			return;
		}
		else {
			for(int i = 0; i < this.prefetchIndex; i++) {
				this.get(i).simpleMatch(context);
				if(context.isFailure()) {
					context.rollback(startIndex);
					return;
				}
			}
			int mark = context.markObjectStack();
			ParsingObject newnode = context.newParsingObject(startIndex, this);
			context.left = newnode;
			if(this.leftJoin) {
				context.logLink(newnode, -1, left);
			}
			for(int i = this.prefetchIndex; i < this.size(); i++) {
				this.get(i).simpleMatch(context);
				if(context.isFailure()) {
					context.abortLinkLog(mark);
					context.rollback(startIndex);
					return;
				}
			}
			context.commitLinkLog(newnode, startIndex, mark);
			if(context.stat != null) {
				context.stat.countObjectCreation();
			}
			context.left = newnode;
			return;
		}
	}
}

class PExport extends PUnary {
	PExport(int flag, PExpression e) {
		super(flag | PExpression.NoMemo, e);
	}
	@Override
	PExpression dup() {
		return new PExport(flag, inner);
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitExport(this);
	}
	@Override
	boolean checkFirstByte(int ch) {
		return this.inner.checkFirstByte(ch);
	}
	@Override
	public void simpleMatch(ParsingContext context) {

	}
}

abstract class ParsingFunction extends PExpression {
	String funcName;
	ParsingFunction(String funcName, int flag) {
		super(flag);
		this.funcName = funcName;
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitParsingFunction(this);
	}
//	@Override
//	boolean checkFirstByte(int ch) {
//		return this.inner.checkFirstByte(ch);
//	}
	String getParameters() {
		return "";
	}
}

class ParsingIndent extends ParsingFunction {
	ParsingIndent(int flag) {
		super("indent", flag | PExpression.HasContext);
	}
	@Override PExpression dup() {
		return this;
	}
	@Override
	boolean checkFirstByte(int ch) {
		return (ch == '\t' || ch == ' ');
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opIndent();
	}
}

class ParsingFail extends ParsingFunction {
	String message;
	ParsingFail(int flag, String message) {
		super("fail", flag);
		this.message = message;
	}
	@Override PExpression dup() {
		return new ParsingFail(flag, message);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opFailure(this.message);
	}
}

class ParsingCatch extends ParsingFunction {
	ParsingCatch(int flag) {
		super("catch", flag);
	}
	@Override PExpression dup() {
		return this;
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opCatch();
	}
}

abstract class ParsingOperation extends PExpression {
	String funcName;
	PExpression inner;
	protected ParsingOperation(String funcName, PExpression inner) {
		super(inner.flag);
		this.funcName = funcName;
		this.inner = inner;
	}
	@Override
	protected void visit(ParsingExpressionVisitor visitor) {
		visitor.visitParsingOperation(this);
	}
	@Override
	boolean checkFirstByte(int ch) {
		return this.inner.checkFirstByte(ch);
	}
	@Override
	public PExpression getExpression() {
		return this.inner;
	}
	public String getParameters() {
		return "";
	}
}

class ParsingMemo extends ParsingOperation {
	static ParsingObject NonTransition = new ParsingObject(null, null, 0);

	boolean enableMemo = true;
	int memoId;
	int memoHit = 0;
	int memoMiss = 0;

	ParsingMemo(int memoId, PExpression inner) {
		super("memo", inner);
		this.memoId = memoId;
	}

	@Override PExpression dup() {
		return new ParsingMemo(0, inner);
	}

	@Override
	public void simpleMatch(ParsingContext context) {
		if(!this.enableMemo) {
			this.inner.simpleMatch(context);
			return;
		}
		long pos = context.getPosition();
		ParsingObject left = context.left;
		ObjectMemo m = context.getMemo(this, pos);
		if(m != null) {
			this.memoHit += 1;
			context.setPosition(pos + m.consumed);
			if(m.generated != NonTransition) {
				context.left = m.generated;
			}
			return;
		}
		this.inner.simpleMatch(context);
		int length = (int)(context.getPosition() - pos);
		context.setMemo(pos, this, (context.left == left) ? NonTransition : context.left, length);
		this.memoMiss += 1;
		this.tryTracing();
	}

	private void tryTracing() {
		if(Main.TracingMemo) {
			if(this.memoMiss == 32) {
				if(this.memoHit < 2) {
					disabledMemo();
					return;
				}
			}
			if(this.memoMiss % 64 == 0) {
				if(this.memoHit == 0) {
					disabledMemo();
					return;
				}
				if(this.memoMiss / this.memoHit > 10) {
					disabledMemo();
					return;
				}
			}
		}		
	}
	
	private void disabledMemo() {
		//this.show();
		this.enableMemo = false;
//		this.base.DisabledMemo += 1;
//		int factor = this.base.EnabledMemo / 10;
//		if(factor != 0 && this.base.DisabledMemo % factor == 0) {
//			this.base.memoRemover.removeDisabled();
//		}
	}

	void show() {
		if(Main.VerboseMode) {
			double f = (double)this.memoHit / this.memoMiss;
			System.out.println(this.inner.getClass().getSimpleName() + " #h/m=" + this.memoHit + "," + this.memoMiss + ", f=" + f + " " + this.inner);
		}
	}
}

class ParsingMatch extends ParsingOperation {
	ParsingMatch(PExpression inner) {
		super("match", inner);
	}
	@Override PExpression dup() {
		return new ParsingMatch(inner);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		boolean oldMode = context.setRecognitionMode(true);
		ParsingObject left = context.left;
		this.inner.simpleMatch(context);
		context.setRecognitionMode(oldMode);
		if(!context.isFailure()) {
			context.left = left;
		}
	}
}

class ParsingStackIndent extends ParsingOperation {
	ParsingStackIndent(PExpression e) {
		super("indent", e);
	}
	@Override PExpression dup() {
		return new ParsingStackIndent(inner);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opPushIndent();
		this.inner.simpleMatch(context);
		context.opPopIndent();
	}
}

class ParsingIfFlag extends ParsingFunction {
	String flagName;
	ParsingIfFlag(int flag, String flagName) {
		super("if", flag | PExpression.HasContext);
		this.flagName = flagName;
	}
	@Override PExpression dup() {
		return new ParsingIfFlag(flag, flagName);
	}
	@Override
	public String getParameters() {
		return " " + this.flagName;
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opCheckFlag(this.flagName);
	}
}

class ParsingWithFlag extends ParsingOperation {
	String flagName;
	ParsingWithFlag(String flagName, PExpression inner) {
		super("with", inner);
		this.flagName = flagName;
	}
	@Override PExpression dup() {
		return new ParsingWithFlag(flagName, inner);
	}
	@Override
	public String getParameters() {
		return " " + this.flagName;
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opEnableFlag(this.flagName);
		this.inner.simpleMatch(context);
		context.opPopFlag(this.flagName);
	}
}

class ParsingWithoutFlag extends ParsingOperation {
	String flagName;
	ParsingWithoutFlag(String flagName, PExpression inner) {
		super("without", inner);
		this.flagName = flagName;
	}
	@Override PExpression dup() {
		return new ParsingWithoutFlag(flagName, inner);
	}
	@Override
	public String getParameters() {
		return " " + this.flagName;
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opDisableFlag(this.flagName);
		this.inner.simpleMatch(context);
		context.opPopFlag(this.flagName);
	}
}

class ParsingDebug extends ParsingOperation {
	protected ParsingDebug(PExpression inner) {
		super("debug", inner);
	}
	@Override PExpression dup() {
		return new ParsingDebug(inner);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
		context.opRememberPosition();
		context.opRememberFailurePosition();
		context.opStoreObject();
		this.inner.simpleMatch(context);
		context.opDebug(this.inner);
	}
}

class ParsingApply extends ParsingOperation {
	ParsingApply(PExpression inner) {
		super("|apply", inner);
	}
	@Override PExpression dup() {
		return new ParsingApply(inner);
	}
	@Override
	public void simpleMatch(ParsingContext context) {
//		ParsingContext s = new ParsingContext(context.left);
//		
//		this.inner.simpleMatch(s);
//		context.opRememberPosition();
//		context.opRememberFailurePosition();
//		context.opStoreObject();
//		this.inner.simpleMatch(context);
//		context.opDebug(this.inner);
	}
}

