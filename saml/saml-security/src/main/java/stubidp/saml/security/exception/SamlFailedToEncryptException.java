package stubidp.saml.security.exception;

import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

public class SamlFailedToEncryptException extends SamlTransformationErrorException {

    public SamlFailedToEncryptException(String errorMessage, Exception cause, Level logLevel) {
        super(errorMessage, cause, logLevel);
    }

    public SamlFailedToEncryptException(String errorMessage, Level logLevel) {
        super(errorMessage, logLevel);
    }

    public SamlFailedToEncryptException(SamlValidationSpecificationFailure failure, Exception cause) {
        super(failure.getErrorMessage(), cause, failure.getLogLevel());
    }
}