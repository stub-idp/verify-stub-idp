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
    public void setup() {
        validator = new AssertionValidator(issuerValidator, subjectValidator, assertionAttributeStatementValidator, basicAssertionSubjectConfirmationValidator);
    }

    @Test
    public void validate_shouldDelegateSubjectValidation() throws Exception {
        String requestId = UUID.randomUUID().toString();
        Assertion assertion = anAssertion()
                .withSubject(aSubject().build())
                .buildUnencrypted();

        validator.validate(assertion, requestId, "");

        verify(subjectValidator).validate(assertion.getSubject(), assertion.getID());
    }

    @Test
    public void validate_shouldDelegateSubjectConfirmationValidation() throws Exception {
        String requestId = UUID.randomUUID().toString();
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().build();
        Assertion assertion = anAssertion()
                .withSubject(aSubject().withSubjectConfirmation(subjectConfirmation).build())
                .buildUnencrypted();

        validator.validate(assertion, requestId, "");

        verify(basicAssertionSubjectConfirmationValidator).validate(subjectConfirmation);
    }

    @Test
    public void validate_shouldDelegateAttributeValidation() throws Exception {
        String requestId = UUID.randomUUID().toString();
        Assertion assertion = anAssertion()
                .withSubject(aSubject().build())
                .buildUnencrypted();

        validator.validate(assertion, requestId, "");

        verify(assertionAttributeStatementValidator).validate(assertion);
    }

    @Test
    public void validate_shouldThrowExceptionIfAnyAssertionDoesNotContainASignature() throws Exception {
        String someID = UUID.randomUUID().toString();
        Assertion assertion = anAssertion().withSignature(null).withId(someID).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.assertionSignatureMissing(someID));
    }

    @Test
    public void validate_shouldThrowExceptionIfAnAssertionIsNotSigned() throws Exception {
        String someID = UUID.randomUUID().toString();

        Assertion assertion = anAssertion().withoutSigning().withId(someID).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.assertionNotSigned(someID));
    }

    @Test
    public void validate_shouldDoNothingIfAllAssertionsAreSigned() throws Exception {
        Assertion assertion = anAssertion().buildUnencrypted();

        validator.validate(assertion, "", assertion.getID());
    }

    @Test
    public void validate_shouldThrowExceptionIfIdIsMissing() throws Exception {
        Assertion assertion = anAssertion().withId(null).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.missingId());
    }

    @Test
    public void validate_shouldThrowExceptionIfVersionIsMissing() throws Exception {
        Assertion assertion = anAssertion().withVersion(null).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.missingVersion(assertion.getID()));
    }

    @Test
    public void validate_shouldThrowExceptionIfVersionIsNotSamlTwoPointZero() throws Exception {
        Assertion assertion = anAssertion().withVersion(SAMLVersion.VERSION_10).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.illegalVersion(assertion.getID()));
    }

    @Test
    public void validate_shouldThrowExceptionIfIssueInstantIsMissing() throws Exception {
        Assertion assertion = anAssertion().withIssueInstant(null).buildUnencrypted();

        assertExceptionMessage(assertion, SamlTransformationErrorFactory.missingIssueInstant(assertion.getID()));
    }

    private void assertExceptionMessage(
            final Assertion assertion,
            SamlValidationSpecificationFailure failure) {

        SamlTransformationErrorManagerTestHelper.validateFail(
                new SamlTransformationErrorManagerTestHelper.Action() {
                    @Override
                    public void execute() {
                        validator.validate(assertion, "", "");
                    }
                },
                failure
        );
    }
}
