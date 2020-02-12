package stubidp.shared.exceptions;

import static java.text.MessageFormat.format;

public class NoAssertionConsumerServiceForEntityException extends RuntimeException {
    public NoAssertionConsumerServiceForEntityException(String expectedEntityId) {
        super(format("No assertion consumer service found for {0}", expectedEntityId));
    }
}
