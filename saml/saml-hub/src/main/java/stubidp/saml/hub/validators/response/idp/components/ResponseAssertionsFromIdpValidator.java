package stubidp.saml.hub.validators.response.idp.components;

import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.core.validators.assertion.IdentityProviderAssertionValidator;
import stubidp.saml.hub.core.validators.assertion.MatchingDatasetAssertionValidator;
import stubidp.saml.hub.core.validators.assertion.AuthnStatementAssertionValidator;
import stubidp.saml.hub.core.validators.assertion.IPAddressValidator;
import stubidp.saml.hub.exception.SamlValidationException;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;

public class ResponseAssertionsFromIdpValidator {

    private final IdentityProviderAssertionValidator identityProviderAssertionValidator;
    private final MatchingDatasetAssertionValidator matchingDatasetAssertionValidator;
    private final AuthnStatementAssertionValidator authnStatementAssertionValidator;
    private final IPAddressValidator ipAddressValidator;
    private final String hubEntityId;

    public ResponseAssertionsFromIdpValidator(IdentityProviderAssertionValidator assertionValidator,
                                              MatchingDatasetAssertionValidator matchingDatasetAssertionValidator,
                                              AuthnStatementAssertionValidator authnStatementAssertionValidator,
                                              IPAddressValidator ipAddressValidator,
                                              String hubEntityId) {
        this.identityProviderAssertionValidator = assertionValidator;
        this.matchingDatasetAssertionValidator = matchingDatasetAssertionValidator;
        this.authnStatementAssertionValidator = authnStatementAssertionValidator;
        this.ipAddressValidator = ipAddressValidator;
        this.hubEntityId = hubEntityId;
    }

    public void validate(ValidatedResponse validatedResponse, ValidatedAssertions validatedAssertions) {
        validatedAssertions.getAssertions().forEach(
            assertion -> identityProviderAssertionValidator.validate(assertion, validatedResponse.getInResponseTo(), hubEntityId)
        );

        if (!validatedResponse.isSuccess()) return;

        Assertion matchingDatasetAssertion = getMatchingDatasetAssertion(validatedAssertions);
        Assertion authnStatementAssertion = getAuthnStatementAssertion(validatedAssertions);

        if (authnStatementAssertion.getAuthnStatements().size() > 1) {
            throw new SamlValidationException(SamlTransformationErrorFactory.multipleAuthnStatements());
        }

        matchingDatasetAssertionValidator.validate(matchingDatasetAssertion, validatedResponse.getIssuer().getValue());
        authnStatementAssertionValidator.validate(authnStatementAssertion);
        identityProviderAssertionValidator.validateConsistency(authnStatementAssertion, matchingDatasetAssertion);
        ipAddressValidator.validate(authnStatementAssertion);
    }

    private Assertion getAuthnStatementAssertion(ValidatedAssertions validatedAssertions) {
        return validatedAssertions.getAuthnStatementAssertion().orElseThrow(() -> new SamlValidationException(SamlTransformationErrorFactory.missingAuthnStatement()));
    }

    private Assertion getMatchingDatasetAssertion(ValidatedAssertions validatedAssertions) {
        return validatedAssertions.getMatchingDatasetAssertion().orElseThrow(() -> new SamlValidationException(SamlTransformationErrorFactory.missingMatchingMds()));
    }
}
