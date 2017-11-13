package cadets1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkSearch {
	/** Matches on things that look like the name element, yielding a name group */
	private static final Pattern NAME_PATTERN = Pattern.compile("<span itemprop='name'>([^<]+)");
	public static WebRegex searcher;

	public static void main(String[] args) {
		searcher = new WebRegex(String.format("ecs_intra_session=%s", getLine("Session cookie")));
		final String id = getLine("Enter iSolutions ID to search");
		final int maxDepth = Integer.parseInt(getLine("Max depth"));

		Person root;
		try {
			// Grab person's name from the page
			Matcher m = searcher.search(NAME_PATTERN, "https://secure.ecs.soton.ac.uk/people/%s", id);
			if(m.find()) {
				root = new Person(id, m.group(1)); // Deserialize person
			} else {throw new IOException();}
		} catch(IOException e) {
			// If a name could not be found or the page had a connection problem, return
			System.err.println("The user does not appear to be valid.");
			return;
		}

		// Do the work
		System.out.println("Building network...");
		new NetworkPopulator(root, maxDepth).populate();
		// Display our results
		System.out.println();
		System.out.println(root);
	}

	/** Used by {@link #getLine(String)} to read stdin */
	private static final BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
	/** Asks a question of the user and returns an answer.<br>
	 * If the user replies with nothing, the question will be asked again
	 * @param message The prompt to ask the user, without a trailing colon
	 * @return The user's clean input */
	private static final String getLine(String message) {
		String line = null;

		while(line == null || line.isEmpty()) { // Empty answers not allowed
			System.out.print(message + ": ");

			try {
				line = inReader.readLine();
			} catch(IOException e) {/* Keep asking! */}
		}
		return line.trim(); // Clean up
	}
}
