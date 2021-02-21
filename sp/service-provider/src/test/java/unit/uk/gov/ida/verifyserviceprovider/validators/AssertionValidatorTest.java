package unit.uk.gov.ida.verifyserviceprovider.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Subject;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anAuthnStatement;

@ExtendWith({MockitoExtension.class})
public class AssertionValidatorTest extends OpenSAMLRunner {

    private AssertionValidator validator;

    @Mock
    private InstantValidator instantValidator;
    @Mock
    private SubjectValidator subjectValidator;
    @Mock
    private ConditionsValidator conditionsValidator;
    @Mock
    private Assertion assertion;

    @BeforeEach
    public void setUp() {
        AuthnStatement authnStatement = mock(AuthnStatement.class);

        validator = new AssertionValidator(
                instantValidator,
                subjectValidator,
                conditionsValidator
        );

        when(assertion.getAuthnStatements()).thenReturn(List.of(authnStatement));
    }

    @Test
    public void shouldValidateAssertionIssueInstant() {
        Instant issueInstant = Instant.now();
        when(assertion.getIssueInstant()).thenReturn(issueInstant);

        validator.validate(assertion, "any-expected-in-response-to", "any-entity-id");

        verify(instantValidator).validate(issueInstant, "Assertion IssueInstant");
    }

    @Test
    public void shouldValidateAssertionSubject() {
        Subject subject = mock(Subject.class, Answers.RETURNS_DEEP_STUBS);
        when(assertion.getSubject()).thenReturn(subject);

        validator.validate(assertion, "some-expected-in-response-to", "any-entity-id");

        verify(subjectValidator).validate(subject, "some-expected-in-response-to");
    }

    @Test
    public void shouldValidateAssertionConditions() {
        Conditions conditions = mock(Conditions.class);
        when(assertion.getConditions()).thenReturn(conditions);

        validator.validate(assertion, "any-expected-in-response-to", "some-entity-id");

        verify(conditionsValidator).validate(conditions, "some-entity-id");
    }

    @Test
    public void shouldThrowExceptionIfAuthnStatementsIsNull() {
        when(assertion.getAuthnStatements()).thenReturn(null);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validate(assertion, "some-expected-in-response-to", "any-entity-id"))
                .withMessage("Exactly one authn statement is expected.");
    }

    @Test
    public void shouldThrowExceptionIfAuthnStatementsIsEmpty() {
        when(assertion.getAuthnStatements()).thenReturn(Collections.emptyList());

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validate(assertion, "some-expected-in-response-to", "any-entity-id"))
                .withMessage("Exactly one authn statement is expected.");
    }

    @Test
    public void shouldThrowExceptionIfMoreThanOneAuthnStatements() {
        when(assertion.getAuthnStatements()).thenReturn(List.of(
                anAuthnStatement().build(),
                anAuthnStatement().build()
        ));

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validate(assertion, "some-expected-in-response-to", "any-entity-id"))
                .withMessage("Exactly one authn statement is expected.");
    }

    @Test
    public void shouldValidateAssertionAuthnInstant() {
        Instant issueInstant = Instant.now();
        when(assertion.getAuthnStatements().get(0).getAuthnInstant()).thenReturn(issueInstant);

        validator.validate(assertion, "any-expected-in-response-to", "any-entity-id");

        verify(instantValidator).validate(issueInstant, "Assertion AuthnInstant");
    }
}