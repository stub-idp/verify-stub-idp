package stubidp.shared.csrf.exceptions;

import stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter;

import static java.text.MessageFormat.format;

public class CSRFCouldNotValidateSessionException extends RuntimeException {
    public CSRFCouldNotValidateSessionException(AbstractCSRFCheckProtectionFilter.Status status) {
        super(format("reason: {0}", status.name()));
    }
}
