package org.peg4d;

import java.util.TreeMap;

import org.peg4d.expression.NonTerminal;
import org.peg4d.expression.Optimizer;
import org.peg4d.expression.PEG4dTransition;
import org.peg4d.expression.ParsingExpression;

public class ParsingRule {
	public final static int LexicalRule   = 0;
	public final static int ObjectRule    = 1;
	public final static int OperationRule = 1 << 1;
	public final static int ReservedRule  = 1 << 15;
	
	public Grammar  peg;
	public String   localName;
	public ParsingRule definedRule;
	
	String baseName;

	ParsingObject po;
	public int type;
	public ParsingExpression expr;
	
	public int refc = 0;

	public ParsingRule(Grammar peg, String ruleName, ParsingObject po, ParsingExpression e) {
		this.peg = peg;
		this.po = po;
		this.baseName = ruleName;
		this.localName = ruleName;
		this.expr = e;
		this.type = ParsingRule.typeOf(ruleName);
	}
	
	public final String getUniqueName() {
		return this.peg.uniqueRuleName(localName);
	}

	public int minlen = -1;

	public boolean isAlwaysConsumed() {
		return this.checkAlwaysConsumed(null, null);
	}
	
	public final boolean checkAlwaysConsumed(String startNonTerminal, UList<String> stack) {
		if(stack != null && this.minlen != 0 && stack.size() > 0) {
			for(String n : stack) { // Check Unconsumed Recursion
				String uName = this.getUniqueName();
				if(uName.equals(n)) {
					this.minlen = 0;
					break;
				}
			}
		}
		if(minlen == -1) {
			if(stack == null) {
				stack = new UList<String>(new String[4]);
			}
			if(startNonTerminal == null) {
				startNonTerminal = this.getUniqueName();
			}
			stack.add(this.getUniqueName());
			this.minlen = this.expr.checkAlwaysConsumed(startNonTerminal, stack) ? 1 : 0;
			stack.pop();
		}
		return minlen > 0;
	}
	
	public int type2 = PEG4dTransition.BooleanType;
	public int inferExpressionType(UMap<String> visited) {
		if(this.type2 != PEG4dTransition.Undefined) {
			return this.type2;
		}
		String uname = this.getUniqueName();
		if(visited != null) {
			if(visited.hasKey(uname)) {
				this.type2 = PEG4dTransition.BooleanType;
				return this.type2;
			}
		}
		else {
			visited = new UMap<String>();
		}
		visited.put(uname, uname);
		int t = expr.inferPEG4dTranstion(visited);
		assert(t != PEG4dTransition.Undefined);
		if(this.type2 == PEG4dTransition.Undefined) {
			this.type2 = t;
		}
		else {
			assert(type2 == t);
		}
		return this.type2;
	}

	public int inferExpressionType() {
		return this.inferExpressionType(null);
	}
	
	
	@Override
	public String toString() {
		String t = "";
		switch(this.type) {
		case LexicalRule:   t = "boolean "; break;
		case ObjectRule:    t = "Object "; break;
		case OperationRule: t = "void "; break;
		}
		return t + this.localName + "[" + this.minlen + "]" + "=" + this.expr;
	}
	
	
	public final void report(ReportLevel level, String msg) {
		if(this.po != null) {
			Main._PrintLine(po.formatSourceMessage(level.toString(), msg));
		}
		else {
			System.out.println("" + level.toString() + ": " + msg);
		}
	}
	
	Grammar getGrammar() {
		return this.peg;
	}
	
	class PegRuleAnnotation {
		String key;
		ParsingObject value;
		PegRuleAnnotation next;
		PegRuleAnnotation(String key, ParsingObject value, PegRuleAnnotation next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

	PegRuleAnnotation annotation;
	
	public void addAnotation(String key, ParsingObject value) {
		this.annotation = new PegRuleAnnotation(key,value, this.annotation);
	}
	
	public final void testExample1(Grammar peg, ParsingContext context) {
		PegRuleAnnotation a = this.annotation;
		while(a != null) {
			boolean isExample = a.key.equals("example");
			boolean isBadExample = a.key.equals("bad-example");
			if(isExample || isBadExample) {
				boolean ok = true;
				ParsingSource s = ParsingSource.newStringSource(a.value);
				context.resetSource(s, 0);
				context.parse2(peg, this.localName, new ParsingObject(), null);
				//System.out.println("@@ fail? " + context.isFailure() + " unconsumed? " + context.hasByteChar() + " example " + isExample + " " + isBadExample);
				if(context.isFailure() || context.hasByteChar()) {
					if(isExample) ok = false;
				}
				else {
					if(isBadExample) ok = false;
				}
				String msg = ( ok ? "[PASS]" : "[FAIL]" ) + " " + this.localName + " " + a.value.getText();
				if(Main.TestMode && !ok) {	
					Main._Exit(1, "[FAIL] tested " + a.value.getText() + " by " + peg.getRule(this.localName));
				}
				Main.printVerbose("Testing", msg);
			}
			a = a.next;
		}
	}
	
	boolean isObjectType() {
		return this.type == ParsingRule.ObjectRule;
	}

	public final static int typeOf(String ruleName) {
		int start = 0;
		for(;ruleName.charAt(start) == '_'; start++) {
			if(start + 1 == ruleName.length()) {
				return LexicalRule;
			}
		}
		boolean firstUpperCase = Character.isUpperCase(ruleName.charAt(start));
		for(int i = start+1; i < ruleName.length(); i++) {
			char ch = ruleName.charAt(i);
			if(ch == '!') break; // option
			if(Character.isUpperCase(ch) && !firstUpperCase) {
				return OperationRule;
			}
			if(Character.isLowerCase(ch) && firstUpperCase) {
				return ObjectRule;
			}
		}
		return firstUpperCase ? LexicalRule : ReservedRule;
	}

	public static boolean isLexicalName(String ruleName) {
		return typeOf(ruleName) == ParsingRule.LexicalRule;
	}

	public static String toOptionName(ParsingRule rule, boolean lexOnly, TreeMap<String,String> withoutMap) {
		String ruleName = rule.baseName;
		if(lexOnly && !isLexicalName(ruleName)) {
			ruleName = "__" + ruleName.toUpperCase();
		}
		if(withoutMap != null) {
			for(String flag : withoutMap.keySet()) {
				ParsingExpression.containFlag(rule.expr, flag);
				ruleName += "!" + flag;
			}
		}
		return ruleName;
	}
	
	public ParsingExpression resolveNonTerminal() {
		return Optimizer.resolveNonTerminal(this.expr);
	}
	
	public UList<ParsingRule> subRule() {
		UMap<ParsingRule> visitedMap = new UMap<ParsingRule>();
		visitedMap.put(this.getUniqueName(), this);
		makeSubRule(this.expr, visitedMap);
		return visitedMap.values(new ParsingRule[visitedMap.size()]);
	}

	private void makeSubRule(ParsingExpression e, UMap<ParsingRule>  visited) {
		for(int i = 0; i < e.size(); i++) {
			makeSubRule(e.get(i), visited);
		}
		if(e instanceof NonTerminal) {
			NonTerminal ne = (NonTerminal)e;
			assert(e.isInterned());
			String un = ne.getUniqueName();
			ParsingRule memoed = visited.get(un);
			if(memoed == null) {
				memoed = ne.getRule();
				visited.put(un, memoed);
				makeSubRule(memoed.expr, visited);
			}
		}
	}

}
