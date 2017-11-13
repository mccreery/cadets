package cadets4;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
	private final String nickname;
	private final String message;
	private final long timestamp;

	public Message(String nickname, String message, long timestamp) {
		this.nickname = nickname;
		this.message = message;
		this.timestamp = timestamp;
	}

	public String getTimestamp() {
		return new SimpleDateFormat("HH:mm").format(new Date(timestamp * 1000));
	}

	@Override
	public String toString() {
		return "[" + nickname + "] at " + getTimestamp() + ": " + message;
	}
}
