package cadets4;

import static cadets4.CommonUtils.GSON;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientConnection implements Runnable {
	private final Socket socket;
	private final String motd;

	public ClientConnection(String motd, Socket socket) {
		this.socket = socket;
		this.motd = motd;
	}

	@Override
	public void run() {
		System.out.println("Client connected: " + socket.getInetAddress());

		Scanner recv = null;
		PrintWriter send = null;
		try {
			recv = new Scanner(socket.getInputStream());
			recv.useDelimiter(CommonUtils.MESSAGE_DELIM);
			send = new PrintWriter(socket.getOutputStream(), false);

			if(motd != null) {
				send.write(GSON.toJson(new Message("Server", motd, 0)));
				send.write("\n\n");
				send.flush();
			}

			while(!socket.isClosed()) {
				System.out.println("Waiting for a message");
				try {
					if(recv.hasNext()) {
						String next = recv.next();
						System.out.println(GSON.fromJson(recv.next(), Message.class));
						send.write(next);
						send.write("\n\n");
						send.flush();
					}
				} catch(NoSuchElementException e) {socket.close();}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(recv != null) recv.close();
			if(send != null) send.close();
			System.out.println("Connection terminated.");
		}
	}
}
