package stubidp.saml.hub.core.validators.assertion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.core.validators.subject.AssertionSubjectValidator;
import stubidp.saml.hub.core.validators.subjectconfirmation.BasicAssertionSubjectConfirmationValidator;
import stubidp.saml.security.validators.issuer.IssuerValidator;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;

@ExtendWith(MockitoExtension.class)
public class AssertionValidatorTest extends OpenSAMLRunner {

    @Mock
    private AssertionSubjectValidator subjectValidator;
    @Mock
    private IssuerValidator issuerValidator;
    @Mock
    private AssertionAttributeStatementValidator assertionAttributeStatementValidator;
    @Mock
    private BasicAssertionSubjectConfirmationValidator basicAssertionSubjectConfirmationValidator;

    private AssertionValidator validator;

    @BeforeEach
    void setup() {
        validator = new AssertionValidator(issuerValidator, subjectValidator, assertionAttributeStatementValidator, basicAssertionSubjectConfirmationValidator);
    }

    @Test
    void validate_shouldDelegateSubjectValidation() {
        String requestId = UUID.randomUUID().toString();
        Assertion assertion = anAssertion()
                .withSubject(aSubject().build())
                .buildUnencrypted();

        validator.validate(assertion, requestId, "");

        verify(subjectValidator).validate(assertion.getSubject(), assertion.getID());
    }

    @Test
    void validate_shouldDelegateSubjectConfirmationValidation() {
        String requestId = UUID.randomUUID().toString();
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().build();
        Assertion assertion = anAssertion()
                .withSubject(aSubject().withSubjectConfirmation(subjectConfirmation).build())
                .buildUnencrypted();

        validator.validate(assertion, requestId, "");

        verify(basicAssertionSubjectConfirmationValidator).validate(subjectConfirmation);
    }

    @Test
    void validate_shouldDelegateAttributeValidation() {
        String requestId = UUID.randomUUID().toString();
        Assertion assertion = anAssertion()
                .withSubject(aSubject().build())
                .buildUnencrypted();

        validator.validate(assertion, requestId, "");

        verify(assertionAttributeStatementValidator).validate(assertion);
    }

    @Test
    void validate_shouldThrowExceptionIfAnyAssertionDoesNotContainASignature() {
        String someID = UUID.randomUUID().toString();
        Assertion assertion = anAssertion().withSignature(null).withId(someID).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.assertionSignatureMissing(someID));
    }

    @Test
    void validate_shouldThrowExceptionIfAnAssertionIsNotSigned() {
        String someID = UUID.randomUUID().toString();

        Assertion assertion = anAssertion().withoutSigning().withId(someID).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.assertionNotSigned(someID));
    }

    @Test
    void validate_shouldDoNothingIfAllAssertionsAreSigned() {
        Assertion assertion = anAssertion().buildUnencrypted();

        validator.validate(assertion, "", assertion.getID());
    }

    @Test
    void validate_shouldThrowExceptionIfIdIsMissing() {
        Assertion assertion = anAssertion().withId(null).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.missingId());
    }

    @Test
    void validate_shouldThrowExceptionIfVersionIsMissing() {
        Assertion assertion = anAssertion().withVersion(null).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.missingVersion(assertion.getID()));
    }

    @Test
    void validate_shouldThrowExceptionIfVersionIsNotSamlTwoPointZero() {
        Assertion assertion = anAssertion().withVersion(SAMLVersion.VERSION_10).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.illegalVersion(assertion.getID()));
    }

    @Test
    void validate_shouldThrowExceptionIfIssueInstantIsMissing() {
        Assertion assertion = anAssertion().withIssueInstant(null).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.missingIssueInstant(assertion.getID()));
    }

    private void assertExceptionMessage(
            final Assertion assertion,
            SamlValidationSpecificationFailure failure) {

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(assertion, "", ""),
                failure
        );
    }
}
