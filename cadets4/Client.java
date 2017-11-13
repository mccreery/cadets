package cadets4;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import static cadets4.CommonUtils.GSON;

public class Client {
	public static void main(String[] args) throws UnknownHostException, IOException {
		InetSocketAddress host = negotiateHost(args);
		System.out.println("Attempting connection to host " + host);

		Socket socket = new Socket();
		socket.connect(host);
		System.out.println("Connected");

		System.out.print("Please select a nickname: ");
		Scanner stdin = new Scanner(System.in);
		String name = "Anon";
		try {
			name = stdin.nextLine().trim();
		} catch(Exception e) {}
		System.out.println("Using nickname " + name);

		PrintWriter send = new PrintWriter(socket.getOutputStream(), false);
		Scanner recv = new Scanner(socket.getInputStream());
		recv.useDelimiter(CommonUtils.MESSAGE_DELIM);

		while(socket.isConnected()) {
			System.out.println("Entering receive block");
			if(recv.hasNext()) {
				System.out.println(GSON.fromJson(recv.next(), Message.class));
			}

			System.out.println("Entering send block");
			if(stdin.hasNextLine()) {
				Message message = new Message(name, stdin.nextLine(), new Date().getTime() / 1000);

				send.write(GSON.toJson(message));
				send.write("\n\n");
				send.flush();
			}
		}

		stdin.close();
		send.close();
		recv.close();
		socket.close();
	}

	/** Chooses the user's desired host based on arguments or fallback input
	 * @param args The arguments supplied to the program
	 * @return An internet address, chosen by the user if possible */
	public static InetSocketAddress negotiateHost(String[] args) {
		String host = null;
		String[] passThrough = Arrays.copyOf(args, args.length + 2);
		passThrough[args.length] = "-p";

		for(int i = 0; i < args.length; i++) {
			if(args[i].startsWith("-")) {
				i++; // Skip one
			} else if(args[i] != null && !args[i].isEmpty() && !args[i].startsWith("-")) {
				host = args[i];
				break;
			}
		}

		if(host == null) {
			host = "localhost";
		}

		int colon = host.indexOf(':');
		if(colon != -1) {
			host = host.substring(0, colon);
			passThrough[args.length + 1] = args[0].substring(colon + 1);
		}
		return new InetSocketAddress(host, CommonUtils.negotiatePort(passThrough));
	}
}
