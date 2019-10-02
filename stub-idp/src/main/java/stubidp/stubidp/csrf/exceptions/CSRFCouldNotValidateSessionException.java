package stubidp.stubidp.csrf.exceptions;

import stubidp.stubidp.csrf.CSRFCheckProtectionFilter;

import static java.text.MessageFormat.format;

public class CSRFCouldNotValidateSessionException extends RuntimeException {
    public CSRFCouldNotValidateSessionException(CSRFCheckProtectionFilter.Status status) {
        super(format("reason: {0}", status.name()));
    }
}
