package stubidp.saml.hub.exception;

import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

public class SamlValidationException extends SamlTransformationErrorException {
    public SamlValidationException(SamlValidationSpecificationFailure failure) {
        super(failure.getErrorMessage(), failure.getLogLevel());
    }
}
