package org.peg4d.infer;

import org.peg4d.query.ArgumentsParser;

public class Main {
	public static void main(String args[]) {
		Options options = Options.createFromCommandLineArguments(args);
		System.out.println(options);
		Engine engine = new Engine(options.getGrammar(), options.isVerbose());
		Format fmt = null;
		if (options.getOutputFileName() != null) {
			fmt = engine.infer(options.getTarget());
			switch (options.getGenType()) {
			case PEGJS:
				engine.outputPegjs(options.getOutputFileName(), fmt);	
				break;
			case NEZ:
				engine.outputNez(options.getOutputFileName(), fmt);	
				break;
			default:
				throw new RuntimeException();
			}
		}
		else {
			fmt = engine.infer(options.getTarget());
			engine.output(System.out, fmt);
		}
		if (options.getLogFileName() != null) {
			long bestTime = Long.MAX_VALUE;
			long t0 = System.currentTimeMillis();
			Statistic stat = null, tmp = null;
			for(int i = 0; i < 10; i++) {
				tmp = new Statistic();
				long t1 = System.currentTimeMillis();
				engine.infer(options.getTarget(), tmp);
				long t2 = System.currentTimeMillis();
				long t = t2 - t1;
				tmp.setWholeTime(t);
				System.out.println("ErapsedTime : " + t + "ms");
				if(t < bestTime) {
					bestTime = t;
					stat = tmp;
				}
				if(t2 - t0 > 200000) break;
			}
			System.out.println("BestTime : " + bestTime + "ms");
			System.out.println(stat);
			stat.output(options.getLogFileName());
		}
		return;
	}
}

class Options {
	enum GenType {
		PEGJS, NEZ
	}
	
	private String grammar = null;
	private String outputFileName = null;
	private GenType genType = GenType.NEZ;
	private String logFileName = null;
	private String target = null;
	private boolean verbose = false;

	private String cache = null;
	
	private Options() {	
	}
	
	static public Options createFromCommandLineArguments(String args[]) {
		Options newOptions = new Options();
		ArgumentsParser argsParser = new ArgumentsParser();
		argsParser.addDefaultAction(s -> argsParser.printHelpBeforeExit(System.err, 1))
		.addHelp("h", "help", false, "show this help message", 
				s -> argsParser.printHelpBeforeExit(System.out, 0))
		.addOption("v", "verbose", false, "verbose debug info",
				s -> newOptions.verbose = true)
		.addOption("js", "pegjs", false, "set output file type to pegjs",
				s -> newOptions.genType = GenType.PEGJS)
		.addOption("g", "grammar", true, "peg definition of target data format", true,
				s -> newOptions.grammar = s.get())
		.addOption("o", "output", true, "output file name", false,
				s -> newOptions.outputFileName = s.get())
		.addOption("l", "log", true, "generate statistic log",
				s -> newOptions.logFileName = s.get())
		.addOption("t", "target", true, "target data file", true,
				s -> newOptions.target = s.get());
		try {
			argsParser.parseAndInvokeAction(args);
		}
		catch(IllegalArgumentException e) {
			System.err.println(e.getMessage());
			argsParser.printHelpBeforeExit(System.err, 1);
		}
		StringBuilder builder = new StringBuilder();
		builder.append("grammar:");
		builder.append(newOptions.grammar);
		builder.append(", ");
		builder.append("target:");
		builder.append(newOptions.target);
		builder.append(", ");
		builder.append("output:");
		builder.append(newOptions.outputFileName);
		builder.append(", ");
		builder.append("log:");
		builder.append(newOptions.logFileName);
		builder.append(", ");
		builder.append("verbose:");
		builder.append(newOptions.verbose);
		newOptions.cache = builder.toString();

		return newOptions; 
	}
	
	@Override
	public String toString() {
		return this.cache;
	}

	public String getGrammar() {
		return this.grammar;
	}

	public String getOutputFileName() {
		return this.outputFileName;
	}
	
	public GenType getGenType() {
		return this.genType;
	}

	public String getLogFileName() {
		return this.logFileName;
	}

	public String getTarget() {
		return this.target;
	}
	
	public boolean isVerbose() {
		return this.verbose;
	}
}