package stubidp.saml.serializers.errors;

import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.extensions.validation.errors.SamlValidationSpecification;

public final class SamlTransformationErrorFactory {

    private SamlTransformationErrorFactory() {
    }

    public static SamlValidationSpecificationFailure unableToDeserializeStringToOpenSaml(final String message) {
        return new SamlValidationSpecification(SamlValidationSpecification.DESERIALIZATION_ERROR, message);
    }

    public static SamlValidationSpecificationFailure unableToUnmarshallElementToOpenSaml(final String elementName) {
        return new SamlValidationSpecification(SamlValidationSpecification.UNMARSHALLING_ERROR, elementName);
    }

    public static SamlValidationSpecificationFailure invalidBase64Encoding(final String input) {
        return new SamlValidationSpecification(SamlValidationSpecification.INVALID_BASE64_ENCODING, input);
    }

    public static SamlValidationSpecificationFailure noSamlMessage() {
        return new SamlValidationSpecification(SamlValidationSpecification.MISSING_SAML);
    }
}
