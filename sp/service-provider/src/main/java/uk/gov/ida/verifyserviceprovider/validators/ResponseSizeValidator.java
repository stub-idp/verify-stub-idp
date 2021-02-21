package uk.gov.ida.verifyserviceprovider.validators;

import stubidp.saml.serializers.deserializers.validators.SizeValidator;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;

public class ResponseSizeValidator implements SizeValidator {

    private static final int MAX_SAML_RESPONSE_LENGTH = 50000;

    @Override
    public void validate(String input) {
        if (input.length() > MAX_SAML_RESPONSE_LENGTH) {
            throw new SamlResponseValidationException("SAML Response is too long.");
        }
    }
}
