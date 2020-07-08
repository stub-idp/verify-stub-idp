package stubidp.saml.hub.exception;

import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;

public class SamlDuplicateRequestIdException extends SamlTransformationErrorException {
    public SamlDuplicateRequestIdException(String errorMessage, Exception cause, Level logLevel) {
        super(errorMessage, cause, logLevel);
    }

    public SamlDuplicateRequestIdException(String errorMessage, Level logLevel) {
        super(errorMessage, logLevel);
    }
}
