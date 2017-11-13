package cadets4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	public static List<Message> messages = new ArrayList<Message>();

	public static void main(String[] args) throws IOException {
		final int port = CommonUtils.negotiatePort(args);
		System.out.println("Using port " + port);

		String motd = null;
		for(int i = 0; i < args.length; i++) {
			if((args[i].equals("-m") || args[i].equals("--motd")) && ++i < args.length) {
				motd = args[i];
			} else if(args[i].startsWith("-m")) {
				motd = args[i].substring(2);
			}
		}
		System.out.println(motd != null ? "MOTD: " + motd : "No MOTD specified");

		ServerSocket server = new ServerSocket(port);
		System.out.println("Listening for connections...");

		while(server.isBound()) {
			Socket client = server.accept();
			new Thread(new ClientConnection(motd, client)).start();
		}
		server.close();
	}
}
