// Spec: http://www.brouhaha.com/~eric/software/barebones/bare_bones_language_summary.html

package cadets2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import cadets2.Command.Init;

public class BareBones {
	public static void main(String[] args) {
		// Takes source filename as command-line argument
		if(args.length == 0) {
			System.err.println("You must specify a file.");
			return;
		}
		File file = new File(args[0]);
		if(!file.isFile()) {
			System.err.println("Invalid file.");
			return;
		}

		// Initialisation
		Command.register();
		Program program = new Program();

		// Inject preinit statements into the program from the command line
		for(int i = 1; i < args.length; i++) {
			int delim = args[i].indexOf('=');
			String name = args[i].substring(0, delim);

			if(isVar(name)) {
				try {
					program.commands.add(new Init(program.commands.size(), name, Integer.parseInt(args[i].substring(delim + 1))));
				} catch(NumberFormatException e) {/* Doesn't matter, ignore */}
			}
		}
		if(program.commands.size() > 0) System.out.println("Injected " + program.commands.size() + " inits from command line");

		// Tokenise source file
		try {
			Scanner scanner = new Scanner(new FileInputStream(file));
			// Regex below is used to break the file into words and symbols (;=#) with any whitespace
			Pattern pattern = Pattern.compile("(?=[;\\=#\\<\\>])|(?<=[;\\=#\\<\\>])\\p{javaWhitespace}*|\\p{javaWhitespace}+");
			scanner.useDelimiter(pattern);

			List<String> tokens = new ArrayList<String>();

			// Keep looking until the end of the file
			while(scanner.hasNext()) {
				String token = scanner.next();

				// Ignore comment lines by telling the scanner to move on and discarding the rest of the line
				if(token.equals("#")) {
					scanner.nextLine();
				} else if(token.equals(";")) { // Flush token buffer to create a new command (EOL)
					program.processLine(tokens);
					tokens.clear();
				} else { // We have a new token for the next command
					tokens.add(token);
				}
			}
			if(tokens.size() != 0) System.err.println("Trailing tokens: " + tokens); // Any tokens left over without a ; are ignored, but warned about
			scanner.close();
		} catch(IOException e) {
			System.err.println("Unable to read file.");
			e.printStackTrace();
			return;
		}

		// Report the result of building the program
		System.out.println("Program has " + program.commands.size() + " total commands");
		program.run();
		program.printVars();
	}

	/** A list of the reserved keywords to check against */
	private static final List<String> RESERVED = Arrays.asList(new String[] {
		"clear", "copy", "decr", "do", "end",
		"incr", "init", "not", "to", "while"
	});
	/** Variable name matching pattern */
	private static final Pattern VARIABLE = Pattern.compile("[A-Z][A-Z0-9_]*", Pattern.CASE_INSENSITIVE);

	/** Validates variable names using the rules:
	 * <ul>
	 *  <li>Variables must start with a letter;</li>
	 *  <li>Variables can contain any sequence of letters, numbers and underscores;</li>
	 *  <li>Variables are case-insensitive;</li>
	 *  <li><strong>clear, copy, decr, do, end, incr, init, not, to</strong> and <strong>while</strong> are all reserved and must not be used.</li>
	 * </ul>
	 * @param key The variable name in question
	 * @return {@code true} if {@code key} is valid */
	public static boolean isVar(String key) {
		return key != null && VARIABLE.matcher(key).matches()
			&& !RESERVED.contains(key.toLowerCase());
	}
}
