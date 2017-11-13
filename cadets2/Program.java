package cadets2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Program {
	/** A mapping of the names and values of each variable in the running program */
	private final Map<String, Integer> vars = new HashMap<String, Integer>();
	/** A list of instructions which make up the program */
	public final List<Command> commands = new ArrayList<Command>();

	/** Displays the values of each variable in the program */
	public void printVars() {
		if(vars.size() == 0) return;
		StringBuilder builder = new StringBuilder(vars.size() * 6);

		for(Entry<String, Integer> entry : vars.entrySet()) {
			// VARIABLE: 1, VARIABLE2: 3
			builder.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
		}
		builder.setLength(builder.length() - 2);
		System.out.println(builder);
	}

	/** Set a variable by the name {@code key} to {@code x}
	 * @param key The variable name
	 * @param x The value to set */
	public void set(String key, int x) { // Variable names are case-insensitive, so we use toUpperCase
		vars.put(key.toUpperCase(), Math.max(0, x));
	}
	/** Get the current value of the variable {@code key}
	 * @param key The variable name
	 * @return The value of {@code key} */
	public int get(String key) { // Variable names are case-insensitive, so we use toUpperCase
		return vars.containsKey(key.toUpperCase()) ? vars.get(key.toUpperCase()) : 0;
	}

	/** Works out which type of command the given string of tokens is
	 * @param tokens Tokens read in from the source file
	 * @see #fromLine(List) */
	public void processLine(List<String> tokens) {
		Command[] command = fromLine(tokens);
		if(command != null) commands.addAll(Arrays.asList(command));
		else System.err.println("Invalid line " + tokens); // None of the commands matched
	}
	/** Chooses a command or set of commands from the list of registered commands which this line matches
	 * @param tokens Tokens read in from the source file */
	private Command[] fromLine(List<String> tokens) {
		for(Command candidate : Command.commands) { // Try to parse this command into every type possible
			Command[] command = candidate.create(this, commands.size(), tokens);

			// We found one!
			if(command != null && command.length > 0) return command;
		}
		return null;
	}

	/** The current location of the program pointer.
	 * Set this outside of the program space to stop the program prematurely */
	public int pointer;

	/** Run the program, iterating each command from start to finish.<br>
	 * This method does not reset the variables! */
	public void run() {
		int steps = 0;
		for(pointer = 0; pointer >= 0 && pointer < commands.size(); pointer++, steps++) {
			commands.get(pointer).execute(this);
		}
		System.out.println("Program halted in " + steps + " steps");
	}
}
