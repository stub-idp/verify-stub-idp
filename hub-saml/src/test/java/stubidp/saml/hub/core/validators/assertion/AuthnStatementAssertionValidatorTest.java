package stubidp.saml.hub.core.validators.assertion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.hub.test.OpenSAMLRunner;
import stubidp.saml.utils.core.test.builders.AssertionBuilder;
import stubidp.saml.utils.core.test.builders.AuthnContextBuilder;
import stubidp.saml.utils.core.test.builders.AuthnContextClassRefBuilder;
import stubidp.saml.utils.core.test.builders.AuthnStatementBuilder;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.authnContextClassRefMissing;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.authnContextClassRefValueMissing;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.authnContextMissingError;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.authnInstantMissing;
import static stubidp.saml.utils.core.test.SamlTransformationErrorManagerTestHelper.validateFail;

@ExtendWith(MockitoExtension.class)
public class AuthnStatementAssertionValidatorTest extends OpenSAMLRunner {

    @Mock
    private DuplicateAssertionValidator duplicateAssertionValidator;

    private AuthnStatementAssertionValidator validator;

    @BeforeEach
    public void setup() {
        validator = new AuthnStatementAssertionValidator(duplicateAssertionValidator);
    }

    @Test
    public void validate_shouldThrowExceptionIfAuthnContextIsAbsent() throws Exception {
        AuthnStatement authnStatement = AuthnStatementBuilder.anAuthnStatement().withAuthnContext(null).build();
        Assertion assertion = AssertionBuilder.anAssertion().addAuthnStatement(authnStatement).buildUnencrypted();

        validateFail(() -> validator.validate(assertion), authnContextMissingError());
    }

    @Test
    public void validate_shouldPassValidation() throws Exception {
        AuthnStatement authnStatement = AuthnStatementBuilder.anAuthnStatement().build();
        Assertion assertion = AssertionBuilder.anAssertion().addAuthnStatement(authnStatement).buildUnencrypted();

        validator.validate(assertion);
    }

    @Test
    public void validate_shouldThrowExceptionIfAuthnContextClassRefIsAbsent() throws Exception {
        AuthnContext authnContext = AuthnContextBuilder.anAuthnContext().withAuthnContextClassRef(null).build();
        AuthnStatement authnStatement = AuthnStatementBuilder.anAuthnStatement().withAuthnContext(authnContext).build();
        Assertion assertion = AssertionBuilder.anAssertion().addAuthnStatement(authnStatement).buildUnencrypted();

        validateFail(() -> validator.validate(assertion), authnContextClassRefMissing());
    }

    @Test
    public void validate_shouldThrowExceptionIfAuthnContextClassRefValueIsAbsent() throws Exception {
        AuthnContextClassRef authnContextClassRef = AuthnContextClassRefBuilder.anAuthnContextClassRef().withAuthnContextClasRefValue(null).build();
        AuthnContext authnContext = AuthnContextBuilder.anAuthnContext().withAuthnContextClassRef(authnContextClassRef).build();
        AuthnStatement authnStatement = AuthnStatementBuilder.anAuthnStatement().withAuthnContext(authnContext).build();
        Assertion assertion = AssertionBuilder.anAssertion().addAuthnStatement(authnStatement).buildUnencrypted();

        validateFail(() -> validator.validate(assertion), authnContextClassRefValueMissing());
    }

    @Test
    public void validate_shouldValidateForDuplicateIds() throws Exception {
        String id = "duplicate-id";
        Assertion assertion = AssertionBuilder.anAssertion().withId(id).addAuthnStatement(AuthnStatementBuilder.anAuthnStatement().build()).buildUnencrypted();

        validator.validate(assertion);

        verify(duplicateAssertionValidator, times(1)).validateAuthnStatementAssertion(assertion);
    }

    @Test
    public void validate_shouldThrowExceptionIfAuthnInstantIsAbsent() throws Exception {
        AuthnStatement authnStatement = AuthnStatementBuilder.anAuthnStatement().withAuthnInstant(null).build();
        Assertion assertion = AssertionBuilder.anAssertion().addAuthnStatement(authnStatement).buildUnencrypted();

        validateFail(() -> validator.validate(assertion), authnInstantMissing());
    }
}
