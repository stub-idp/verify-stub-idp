package stubidp.saml.hub.validators.response.common;

import javax.inject.Inject;

public class ResponseMaxSizeValidator extends ResponseSizeValidator {
    private static final int LOWER_BOUND = 0;

    @Inject
    public ResponseMaxSizeValidator() {
        super();
    }

    @Override
    protected int getLowerBound() {
        return LOWER_BOUND;
    }

}
