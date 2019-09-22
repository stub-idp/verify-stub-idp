package stubidp.saml.utils.hub.transformers.inbound.decorators;

import stubidp.saml.serializers.deserializers.validators.SizeValidator;
import stubidp.saml.utils.hub.validators.StringSizeValidator;

import javax.inject.Inject;

public class AuthnRequestSizeValidator implements SizeValidator {

    private static final int LOWER_BOUND = 1200;
    private static final int UPPER_BOUND = 7 * 1024;

    private final StringSizeValidator validator;

    @Inject
    public AuthnRequestSizeValidator(StringSizeValidator validator) {
        this.validator = validator;
    }

    @Override
    public void validate(String input) {
        validator.validate(input,LOWER_BOUND, UPPER_BOUND);
    }
}
