package org.peg4d.infer;

import java.util.List;

public class FormatFactory {
	private final Engine engine;
	private int count;
	
	FormatFactory(Engine engine) {
		this.engine = engine;
		this.count = 0;
	}
	
	String newName() {
		return "format" + this.count++;
	}
	
	Format create() {
		return EmptyFormat.getInstance();
	}

	Format create(SimpleTokenComparedByType token, List<Token<?>> delimSrc) {
		String[] splitted = token.getType().ruleName.split("_");
		if (splitted.length == 2) {
			return new NonTerminalFormat(token.getType().ruleName, delimSrc, Integer.parseInt(splitted[1]));
		}
		else {
			return new NonTerminalFormat(token.getType().ruleName);
		}
	}
	Format create(SimpleTokenComparedByValue token) {
		return this.create(token.getValue());
	}
	
	Format create(MetaToken token, Branch subBranch) {
		SeqFormat fmt = new SeqFormat(this.newName());
		fmt.add(this.create(token.getLeftDelimiter()));
		fmt.add(this.engine.structureDiscovery(subBranch));
		fmt.add(this.create(token.getRightDelimiter()));
		return fmt;
	}
	
	Format create(String text) {
		return new TerminalFormat(text);
	}
	
	Format create(SplittedBranch splittedBranch) {
		SeqFormat ret = new SeqFormat(this.newName());
		List<? extends Branch> subBranches = splittedBranch.getSubBranches();
		List<Token<?>> delimSeq = splittedBranch.getDelimSeq();
		List<List<Token<?>>> delimSrc = splittedBranch.getDelimSrc();
		int i = 0, j = 0;
		for (Token<?> delim = null; i < delimSeq.size(); i++) {
			ret.add(this.engine.structureDiscovery(subBranches.get(j++)));
			delim = delimSeq.get(i);
			if (delim instanceof SimpleTokenComparedByType) {
				ret.add(this.create((SimpleTokenComparedByType)delim, delimSrc.get(i)));
			}
			else if (delim instanceof SimpleTokenComparedByValue) {
				ret.add(this.create((SimpleTokenComparedByValue)delim));
			}
			else if (delim instanceof MetaToken) {
				ret.add(this.create((MetaToken)delim, subBranches.get(j++)));
			}
			else {
				throw new RuntimeException();
			}
		}
		ret.add(this.engine.structureDiscovery(subBranches.get(j)));
		return ret;
	}
	
	Format create(SplittedBranch splittedBranch, Splitter splitter) {
		switch (splittedBranch.getDirection()) {
		case Vertical:
			return this.create(splittedBranch);
		case Horizontal:
			ChoiceFormat fmt = new ChoiceFormat(this.newName());
			SplittedBranch splittedSubBranch = null;
			for (Branch subBranch : splittedBranch.getSubBranches()) {
				splittedSubBranch = splitter.splitBranch(subBranch);
				if (splittedSubBranch.getDirection() != SplittedBranch.Direction.Vertical) throw new RuntimeException(); 
				fmt.add(this.create(splittedSubBranch));
			}
			return fmt;
		default:
			throw new RuntimeException();
		}
	}

	Format create(List<Branch> groupedBranches) {
		ChoiceFormat fmt = new ChoiceFormat(this.newName());
		if (groupedBranches.size() == 1) {
			for (Chunk chunk : groupedBranches.get(0)) {
				fmt.add(this.create(chunk.getText()));
			}
		}
		else {
			for (Branch branch : groupedBranches) {
				fmt.add(this.engine.structureDiscovery(branch));
			}
		}
		return fmt;
	}
}
