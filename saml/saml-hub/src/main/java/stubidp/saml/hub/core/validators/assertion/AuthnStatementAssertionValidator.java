package stubidp.saml.hub.core.validators.assertion;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.exception.SamlValidationException;

public class AuthnStatementAssertionValidator {
    private final DuplicateAssertionValidator duplicateAssertionValidator;

    public AuthnStatementAssertionValidator(DuplicateAssertionValidator duplicateAssertionValidator) {
        this.duplicateAssertionValidator = duplicateAssertionValidator;
    }

    public void validate(Assertion assertion) {
        validateAuthnStatement(assertion.getAuthnStatements().get(0));
        duplicateAssertionValidator.validateAuthnStatementAssertion(assertion);
    }

    private void validateAuthnStatement(AuthnStatement authnStatement) {
        if (authnStatement.getAuthnContext() == null)
            throw new SamlValidationException(SamlTransformationErrorFactory.authnContextMissingError());
        if (authnStatement.getAuthnContext().getAuthnContextClassRef() == null)
            throw new SamlValidationException(SamlTransformationErrorFactory.authnContextClassRefMissing());
        if (authnStatement.getAuthnContext().getAuthnContextClassRef().getURI() == null)
            throw new SamlValidationException(SamlTransformationErrorFactory.authnContextClassRefValueMissing());
        if (authnStatement.getAuthnInstant() == null)
            throw new SamlValidationException(SamlTransformationErrorFactory.authnInstantMissing());
    }
}
