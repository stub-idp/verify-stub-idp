package stubidp.saml.security.exception;

import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

public class SamlFailedToDecryptException extends SamlTransformationErrorException {

    public SamlFailedToDecryptException(String errorMessage, Exception cause, Level logLevel) {
        super(errorMessage, cause, logLevel);
    }

    public SamlFailedToDecryptException(String errorMessage, Level logLevel) {
        super(errorMessage, logLevel);
    }

    public SamlFailedToDecryptException(SamlValidationSpecificationFailure failure, Exception cause) {
        super(failure.getErrorMessage(), cause, failure.getLogLevel());
    }
}
