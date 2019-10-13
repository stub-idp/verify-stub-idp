package stubidp.shared.exceptions;

import stubidp.utils.rest.common.SessionId;

import java.util.Optional;

public class InvalidSecureCookieException extends RuntimeException {
    private Optional<SessionId> sessionId;
    private final String message;

    public InvalidSecureCookieException(String message, SessionId sessionId) {
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
