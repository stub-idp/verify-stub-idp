package uk.gov.ida.rp.testrp.saml.validators;

import stubidp.saml.utils.hub.validators.StringSizeValidator;

import javax.inject.Inject;

public class NoOpStringSizeValidator extends StringSizeValidator {

    @Inject
    public NoOpStringSizeValidator() {
    }

    @Override
    public void validate(String input, int lowerBound, int upperBound) {
        // do nothing
    }
}
