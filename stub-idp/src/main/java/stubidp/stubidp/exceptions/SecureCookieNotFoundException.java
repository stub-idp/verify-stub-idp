package stubidp.stubidp.exceptions;

import stubidp.utils.rest.common.SessionId;

import java.util.Optional;

public class SecureCookieNotFoundException extends RuntimeException {

    private final Optional<SessionId> sessionId;
    private final String message;

    public SecureCookieNotFoundException(String message, SessionId sessionId) {
        super(message);
        this.message = message;
        this.sessionId = Optional.ofNullable(sessionId);
    }

    public Optional<SessionId> getSessionId() {
        return sessionId;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
