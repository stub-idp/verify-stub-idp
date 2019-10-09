package stubidp.stubidp.exceptions;

import static java.text.MessageFormat.format;

public class UnHashedPasswordException extends RuntimeException {
    public UnHashedPasswordException(String username) {
        super(format("username: {0}", username));
    }
}
