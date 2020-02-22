package stubidp.stubidp.exceptions;

public class SessionSerializationException extends RuntimeException {
	public SessionSerializationException(String message, Exception e) {
		super(message, e);
	}
}
