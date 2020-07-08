package stubidp.saml.utils.hub.errors;

import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.extensions.validation.errors.ResponseProcessingValidationSpecification;
import stubidp.saml.extensions.validation.errors.StringValidationSpecification;

public final class SamlTransformationErrorFactory {

    private SamlTransformationErrorFactory() {
    }

    public static SamlValidationSpecificationFailure stringTooSmall(int length, int lowerBound) {
        return new StringValidationSpecification(StringValidationSpecification.LOWER_BOUND_ERROR_MESSAGE, length, lowerBound);
    }

    public static SamlValidationSpecificationFailure stringTooLarge(int length, int upperBound) {
        return new StringValidationSpecification(StringValidationSpecification.UPPER_BOUND_ERROR_MESSAGE, length, upperBound);
    }

    public static SamlValidationSpecificationFailure missingAttributeStatementInAssertion(final String assertionId) {
        return new ResponseProcessingValidationSpecification(ResponseProcessingValidationSpecification.MISSING_ATTRIBUTE_STATEMENT_IN_ASSERTION, assertionId);
    }

}
