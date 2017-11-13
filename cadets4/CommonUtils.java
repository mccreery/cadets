package cadets4;

import java.util.Scanner;
import java.util.regex.Pattern;

import com.google.gson.Gson;

public final class CommonUtils {
	public static final int PORT = 65535;
	public static final Pattern MESSAGE_DELIM = Pattern.compile("(\\r\\n?|\\n)\\1+");
	public static final Gson GSON = new Gson();

	private CommonUtils() {}

	/** Chooses the user's desired port based on arguments or fallback input
	 * @param args The arguments supplied to the program
	 * @return A valid port, chosen by the user if possible */
	public static int negotiatePort(String[] args) {
		boolean custom = false;
		int port = PORT;

		for(int i = 0; i < args.length; i++) {
			args[i] = args[i].trim();

			try {
				if(args[i].equals("-p") || args[i].equals("--port")) {
					if(++i < args.length && args[i] != null && !args[i].isEmpty() && !args[i].startsWith("-")) {
						port = Integer.parseInt(args[i].trim());
					}
					custom = true;
					break;
				} else if(args[i].startsWith("-p")) {
					port = Integer.parseInt(args[i].substring(2));
					custom = true;
					break;
				}
			} catch(NumberFormatException e) {
				System.err.println("The port specified in the argument at index " + i + " \"" + args[i] + "\" is invalid.");
			}
		}

		if(!custom) {
			Scanner in = new Scanner(System.in);
			System.out.print("Port (leave blank for default): ");
	
			String line = null;
			try {
				port = Integer.parseInt(line = in.nextLine().trim());
				custom = true;
			} catch(NumberFormatException e) {
				if(line != null && !line.isEmpty()) {
					System.err.println("The port \"" + line + "\" is invalid.");
				}
			} finally {
				in.close();
			}
		}

		if(custom && (port < 0 || port > 65535)) {
			System.err.println("Port " + port + " is outside the valid range of 0..65535.");
			port = PORT;
		}
		return port;
	}
}
