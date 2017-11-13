package cadets2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Command {
	/** Storage for the master copies of each command.<br>
	 * When a line is read from the source, these command instances act as factories
	 * for the commands actually run in the program */
	public static final Set<Command> commands = new HashSet<Command>();

	/** Registers all the commands capable of being run by the interpreter
	 * @see #register(Command...) */
	public static void register() {
		register( // Create master copies as factories
			new Increment(0, null),
			new Decrement(0, null),
			new Clear(0, null),
			new While(0, null, null, 0),
			new For(0, null, null, 0, 0),
			new WhileEnd(0),
			new Copy(0, null, null),
			new Init(0, null, 0)
		);
	}
	/** Registers the given commands to be used by the interpreter
	 * @param register All the commands to register at once */
	public static void register(Command... register) {
		commands.addAll(Arrays.asList(register));
	}

	/** The position of this command in the whole program */
	protected int position;
	public Command(int position) {
		this.position = position;
	}
	public int getPosition() {
		return position;
	}

	/** Factory method for attempting to create a command or set of commands from a line of tokens.<br>
	 * Commands should use validation such as {@link BareBones#isVar(String)} to check the line is valid
	 * @param program The current program, used by {@link WhileEnd} to find the corresponding {@link While}
	 * @param position The position of the next command to be added to the program
	 * @param tokens A full line of tokens read from the source, excluding the ;
	 * @return A command object if the input tokens are valid, else {@code null} */
	public abstract Command[] create(Program program, int position, List<String> tokens);

	/** Performs the action of this command on the current program
	 * @param program The current running program */
	public abstract void execute(Program program);

	/** incr, decr and clear are all operations which modify a single variable so this superclass is used
	 * to reduce repitition in their definitions */
	public static abstract class Operator extends Command {
		/** Perform the operation
		 * @param x Input value
		 * @return Output value */
		public abstract int operate(int x);

		/** {@link #name} refers to the name of the operator command, {@link key} refers to the variable name */
		private final String name, key;
		/** @see #name */
		public Operator(int position, String name, String key) {
			super(position);
			this.name = name;
			this.key = key;
		}

		@Override
		public void execute(Program program) {
			// Apply the subclass' implementation of the operator
			program.set(key, operate(program.get(key)));
		}

		/** Checks the tokens against the command syntax
		 * @param tokens The line of tokens
		 * @return {@code true} if the line is valid for this operator */
		protected boolean matches(List<String> tokens) {
			// Only works if the name of the command is correct
			return tokens.size() == 2 && tokens.get(0).equalsIgnoreCase(name) && BareBones.isVar(tokens.get(1));
		}
	}

	// These three classes are simple to write because of the above definition

	/** Increment variable command "incr" */
	public static class Increment extends Operator {
		public Increment(int position, String key) {super(0, "incr", key);}
		@Override
		public int operate(int x) {return x + 1;}

		@Override
		public Command[] create(Program program, int position, List<String> tokens) {
			if(matches(tokens)) return new Command[] {new Increment(position, tokens.get(1))};
			return null;
		}
	}

	/** Decrement variable command "decr" */
	public static class Decrement extends Operator {
		public Decrement(int position, String key) {super(0, "decr", key);}
		@Override
		public int operate(int x) {return x - 1;}

		@Override
		public Command[] create(Program program, int position, List<String> tokens) {
			if(matches(tokens)) return new Command[] {new Decrement(position, tokens.get(1))};
			return null;
		}
	}

	/** Clear variable command "clear" */
	public static class Clear extends Operator {
		public Clear(int position, String key) {super(0, "clear", key);}
		@Override
		public int operate(int x) {return 0;}

		@Override
		public Command[] create(Program program, int position, List<String> tokens) {
			if(matches(tokens)) return new Command[] {new Clear(position, tokens.get(1))};
			return null;
		}
	}

	/** While loop opening command "while" */
	public static class While extends Command {
		/** The end command closing this while block
		 * @see WhileEnd#start */
		private WhileEnd end = null;

		/** The value to test against each loop */
		private final int value;
		/** The type of comparison. {@link Comparison} */
		private final Comparison comp;

		/** The name of the variable to check against */
		protected String key;
		/** @see #key */
		public While(int position, String key, Comparison comp, int value) {
			super(position);
			this.key = key;
			this.comp = comp;
			this.value = value;
		}

		/** Links this while loop to an ending command so the loop can be jumped around */
		public void pair(WhileEnd end) {
			if(!isPaired()) this.end = end;
		}
		/** @return {@code true} if {@link #pair(WhileEnd)} has been called with a non-null value */
		public boolean isPaired() {
			return end != null;
		}

		@Override
		public Command[] create(Program program, int position, List<String> tokens) {
			if(tokens.size() == 5 && tokens.get(0).equalsIgnoreCase("while") && BareBones.isVar(tokens.get(1)) && tokens.get(4).equalsIgnoreCase("do")) {
				Comparison comp = Comparison.fromToken(tokens.get(2));

				if(comp != null) {
					try {
						return new Command[] {new While(position, tokens.get(1), comp, Integer.parseInt(tokens.get(3)))};
					} catch(NumberFormatException e) {}
				}
			}
			return null;
		}

		@Override
		public void execute(Program program) {
			if(end == null) { // If there is no end, the while loop is useless; just move on to the next command
				System.err.println("Skipping unpaired while");
			} else if(!comp.evaluate(program, key, value)) { // Calculate condition and jump if the loop is over
				program.pointer = end.position;
			}
		}

		/** An enum containing different kinds of comparison condition for loops */
		public enum Comparison {
			NOT("not") {public boolean evaluate(int a, int b) {return a != b;}},
			EQUAL("=") {public boolean evaluate(int a, int b) {return a == b;}},
			LESS("<") {public boolean evaluate(int a, int b) {return a < b;}},
			MORE(">") {public boolean evaluate(int a, int b) {return a > b;}};

			private final String token;
			Comparison(String token) {this.token = token;}

			public abstract boolean evaluate(int a, int b);
			public boolean evaluate(Program program, String key, int b) {
				return evaluate(program.get(key), b);
			}

			public static Comparison fromToken(String token) {
				for(Comparison comp : values()) {
					if(token.equalsIgnoreCase(comp.token)) return comp;
				}
				return null;
			}
		}
	}

	/** For loop opening command "for" */
	public static class For extends While {
		private final int start, end;

		// This class only used to insert change
		public For(int position, String key, Comparison comp, int start, int end) {
			super(position, key, comp, end);
			this.start = start;
			this.end = end;
		}

		@Override
		public Command[] create(Program program, int position, List<String> tokens) {
			if(tokens.size() == 7 && tokens.get(0).equalsIgnoreCase("for") && BareBones.isVar(tokens.get(1)) && tokens.get(2).equals("=")
					&& tokens.get(4).equalsIgnoreCase("to") && tokens.get(6).equalsIgnoreCase("do")) {
				try {
					int start = Integer.parseInt(tokens.get(3)), end = Integer.parseInt(tokens.get(5));

					return new Command[] {
						new Init(position, tokens.get(1), start),
						new For(position+1, tokens.get(1), start <= end ? Comparison.LESS : Comparison.MORE, start, end)
					};
				} catch(NumberFormatException e) {}
			}
			return null;
		}

		public Command getChange(int position) {
			return start <= end ? new Increment(position, key) : new Decrement(position, key);
		}
	}

	/** While/for loop closing command "end" */
	public static class WhileEnd extends Command {
		/** The start command at the front of this while block
		 * @see While#end */
		private While start = null;

		public WhileEnd(int position) {
			super(position);
		}

		@Override
		public Command[] create(Program program, int position, List<String> tokens) {
			if(tokens.size() != 1 || !tokens.get(0).equalsIgnoreCase("end")) return null;
			WhileEnd end = new WhileEnd(position);

			/* Attempts to scroll back through the program to find a starting while command,
			 * ignoring any already-closed blocks (which fixes the end attaching itself
			 * to the wrong start */
			int skip = 0;
			for(int i = program.commands.size() - 1; i >= 0; i--) {
				if(program.commands.get(i) instanceof WhileEnd) {
					++skip; // One extra start command is present above
				} else if(program.commands.get(i) instanceof While && skip-- == 0) {
					// If we see a start, our end is accounted for and if we weren't looking for extras to begin with, we found our match
					(end.start = (While)program.commands.get(i)).pair(end); // Make a two-way link between the commands

					if(program.commands.get(i) instanceof For) {
						Command change = ((For)program.commands.get(i)).getChange(position);
						end.position++;
						return new Command[] {change, end};
					} else {
						break;
					}
				}
			}
			return new Command[] {end}; // If we never found a start to match this end, it is left dangling
		}

		@Override
		public void execute(Program program) {
			if(start != null) { // Jump back to the start of the loop
				program.pointer = start.position - 1;
			} else { // An end on its own means absolutely nothing and can just be ignored
				System.err.println("Skipping unpaired end");
			}
		}
	}

	/** Copy a to b command "copy" */
	public static class Copy extends Command {
		/** Variable names to copy from and to */
		private final String a, b;
		/** @see #a */
		public Copy(int position, String a, String b) {
			super(position);
			this.a = a;
			this.b = b;
		}

		@Override
		public Command[] create(Program program, int position, List<String> tokens) {
			if(tokens.size() == 4
					&& tokens.get(0).equalsIgnoreCase("copy") && BareBones.isVar(tokens.get(1))
					&& tokens.get(2).equalsIgnoreCase("to") && BareBones.isVar(tokens.get(3))) {
				return new Command[] {new Copy(position, tokens.get(1), tokens.get(3))};
			}
			return null;
		}

		@Override
		public void execute(Program program) {
			program.set(b, program.get(a)); // Simply copies a to b
		}
	}

	/** Initialise variable command "init" */
	public static class Init extends Command {
		/** The variable name */
		private final String key;
		/** The value to set */
		private final int value;

		/** @see #key
		 * @see #value */
		public Init(int position, String key, int value) {
			super(position);
			this.key = key;
			this.value = value;
		}

		@Override
		public Command[] create(Program program, int position, List<String> tokens) {
			if(tokens.size() == 4 && tokens.get(0).equalsIgnoreCase("init") && BareBones.isVar(tokens.get(1)) && tokens.get(2).equals("=")) {
				try {
					return new Command[] {new Init(position, tokens.get(1), Integer.parseInt(tokens.get(3)))};
				} catch(NumberFormatException e) {}
			}
			return null;
		}

		@Override
		public void execute(Program program) {
			program.set(key, value);
		}
	}
}
