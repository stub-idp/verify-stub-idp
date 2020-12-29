package stubidp.saml.hub.validators.response.matchingservice;

import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.core.validators.assertion.AssertionValidator;
import stubidp.saml.hub.exception.SamlValidationException;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;

public class ResponseAssertionsFromMatchingServiceValidator {

    private final AssertionValidator assertionValidator;
    private final String hubEntityId;

    public ResponseAssertionsFromMatchingServiceValidator(AssertionValidator assertionValidator, String hubEntityId) {
        this.assertionValidator = assertionValidator;
        this.hubEntityId = hubEntityId;
    }

    public void validate(ValidatedResponse validatedResponse, ValidatedAssertions validatedAssertions) {
        if (!validatedResponse.isSuccess()) return;

        for (Assertion assertion : validatedAssertions.getAssertions()) {
            assertionValidator.validate(assertion, validatedResponse.getInResponseTo(), hubEntityId);

            if (assertion.getAuthnStatements().isEmpty()) {
                throw new SamlValidationException(SamlTransformationErrorFactory.missingAuthnStatement());
            }

            if (assertion.getAuthnStatements().get(0).getAuthnContext() == null) {
                throw new SamlValidationException(SamlTransformationErrorFactory.authnContextMissingError());
            }
        }
    }
}
