package stubidp.shared.exceptions;

public class SessionIdCookieNotFoundException extends RuntimeException {
    private final String message;

    public SessionIdCookieNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
