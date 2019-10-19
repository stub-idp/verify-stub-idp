package stubidp.saml.hub.core.validators.assertion;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;import stubidp.saml.hub.core.validators.subject.AssertionSubjectValidator;
import stubidp.saml.hub.core.validators.subjectconfirmation.AssertionSubjectConfirmationValidator;
import stubidp.saml.security.validators.issuer.IssuerValidator;

import java.util.Arrays;
import java.util.List;

public class IdentityProviderAssertionValidator extends AssertionValidator {

    private final AssertionSubjectConfirmationValidator subjectConfirmationValidator;

    public IdentityProviderAssertionValidator(
            IssuerValidator issuerValidator,
            AssertionSubjectValidator subjectValidator,
            AssertionAttributeStatementValidator assertionAttributeStatementValidator,
            AssertionSubjectConfirmationValidator subjectConfirmationValidator) {
        this(issuerValidator, subjectValidator, assertionAttributeStatementValidator, subjectConfirmationValidator, true);
    }

    public IdentityProviderAssertionValidator(
            IssuerValidator issuerValidator,
            AssertionSubjectValidator subjectValidator,
            AssertionAttributeStatementValidator assertionAttributeStatementValidator,
            AssertionSubjectConfirmationValidator subjectConfirmationValidator,
            boolean signedAssertions) {
        super(issuerValidator, subjectValidator, assertionAttributeStatementValidator, subjectConfirmationValidator, signedAssertions);

        this.subjectConfirmationValidator = subjectConfirmationValidator;
    }

    public void validateConsistency(Assertion authnStatementAssertion, Assertion matchingDatasetAssertion) {
        validateConsistency(Arrays.asList(authnStatementAssertion, matchingDatasetAssertion));
    }

    public void validateConsistency(List<Assertion> assertions) {
        ensurePidsMatch(assertions);

        ensureIssuersMatch(assertions);
    }

    private void ensurePidsMatch(List<Assertion> assertions) {
        boolean pidsDoNotMatch = assertions.stream()
            .map(assertion -> assertion.getSubject().getNameID().getValue())
            .distinct()
            .count() > 1;

        if (pidsDoNotMatch) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.mismatchedPersistentIdentifiers();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    private void ensureIssuersMatch(List<Assertion> assertions) {
        boolean issuerValuesDoNotMatch = assertions.stream()
            .map(assertion -> assertion.getIssuer().getValue())
            .distinct()
            .count() > 1;

        if (issuerValuesDoNotMatch) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.mismatchedIssuers();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    @Override
    protected void validateSubject(
            Assertion assertion,
            String requestId,
            String expectedRecipientId) {

        super.validateSubject(assertion, requestId, expectedRecipientId);

        ensurePresenceOfBearerSubjectConfirmation(assertion);

        validateAllBearerSubjectConfirmations(assertion, requestId, expectedRecipientId);

        validateFraudAttribute(assertion);
    }

    private void validateAllBearerSubjectConfirmations(
            Assertion assertion,
            String requestId,
            String expectedRecipientId) {

        for (SubjectConfirmation subjectConfirmation : assertion.getSubject().getSubjectConfirmations()) {
            if (SubjectConfirmation.METHOD_BEARER.equals(subjectConfirmation.getMethod())) {
                subjectConfirmationValidator.validate(subjectConfirmation, requestId, expectedRecipientId);
            }
        }
    }

    private void ensurePresenceOfBearerSubjectConfirmation(Assertion assertion) {
        boolean hasSubjectConfirmationWithBearerMethod = false;
        for (SubjectConfirmation subjectConfirmation : assertion.getSubject().getSubjectConfirmations()) {
            if (SubjectConfirmation.METHOD_BEARER.equals(subjectConfirmation.getMethod())) {
                hasSubjectConfirmationWithBearerMethod = true;
            }
        }

        if (!hasSubjectConfirmationWithBearerMethod) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.noSubjectConfirmationWithBearerMethod(assertion.getID());
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    private void validateFraudAttribute(Assertion assertion) {
        if (assertion.getAuthnStatements().size() == 1){
            AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
            boolean isFraudResponse = authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef().equals(AuthnContext.LEVEL_X.getUri());
            if (isFraudResponse)
            {
                assertionAttributeStatementValidator.validateFraudEvent(assertion);
            }
        }
    }
}
