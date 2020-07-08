package stubidp.saml.hub.exception;

import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;

public class SamlRequestTooOldException extends SamlTransformationErrorException {
    public SamlRequestTooOldException(String errorMessage, Exception cause, Level logLevel) {
        super(errorMessage, cause, logLevel);
    }

    public SamlRequestTooOldException(String errorMessage, Level logLevel) {
        super(errorMessage, logLevel);
    }
}
