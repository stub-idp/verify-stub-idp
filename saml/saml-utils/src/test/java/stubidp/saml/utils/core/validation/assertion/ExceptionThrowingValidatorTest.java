package stubidp.saml.utils.core.validation.assertion;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExceptionThrowingValidatorTest {

    @Test
    void shouldCatchValidationException() {
        ExceptionThrowingValidator<Assertion> validator = e -> {
            throw new ExceptionThrowingValidator.ValidationException("message", new SamlResponseValidationException("message"));
        };

        assertThrows(ExceptionThrowingValidator.ValidationException.class, () -> validator.apply(null));
    }

    @Test
    void shouldPropagateARuntimeExceptionOutsideLambdaIfNotCaughtInLambda() {
        ExceptionThrowingValidator<Assertion> validator = e -> {
            throw new SamlResponseValidationException("message");
        };

        assertThrows(RuntimeException.class, () -> validator.apply(null));
    }
}