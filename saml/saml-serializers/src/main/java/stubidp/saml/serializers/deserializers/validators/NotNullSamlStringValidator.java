package stubidp.saml.serializers.deserializers.validators;

import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.serializers.errors.SamlTransformationErrorFactory;

public class NotNullSamlStringValidator {

    public NotNullSamlStringValidator() {
    }

    public void validate(String input) {
        if (input == null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.noSamlMessage();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }
}
