package stubidp.shared.exceptions;

import static java.text.MessageFormat.format;

public class NoSingleSignOnServiceForEntityException extends RuntimeException {
    public NoSingleSignOnServiceForEntityException(String expectedEntityId) {
        super(format("No single sign on found for {0}", expectedEntityId));
    }
}
