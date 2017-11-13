package cadets1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Searches the source of a given web page for a regular expression */
public class WebRegex {
	/** The authentication cookie to send with all requests */
	private final String cookie;

	/** @param cookie The authentication cookie to send with all requests
	 * @see WebRegex */
	public WebRegex(String cookie) {
		this.cookie = cookie;
	}

	/** Search the given web page with the given arguments for the given pattern
	 * @param pattern A regular expression to look for
	 * @param format The URL query string, in {@link String#format(String, Object...)} style
	 * @param args Substitutions for {@link String#format(String, Object...)} */
	public Matcher search(Pattern pattern, String format, Object... args) throws IOException {
		HttpURLConnection conn = createConnection(format, args);
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		// Read page
		StringBuilder builder = new StringBuilder();
		for(String s; (s = reader.readLine()) != null; builder.append(s));
		// Match patterns
		return pattern.matcher(builder);
	}

	/** Creates a secure connection with authentication cookie
	 * @see #search(Pattern, String, Object...) */
	private HttpURLConnection createConnection(String format, Object... args) throws IOException {
		URL url = new URL(String.format(format, args));

		// Open connection
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("Cookie", cookie);
		return conn;
	}
}
