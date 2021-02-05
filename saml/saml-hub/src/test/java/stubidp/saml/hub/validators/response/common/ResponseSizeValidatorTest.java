package stubidp.saml.hub.validators.response.common;

import org.junit.jupiter.api.Test;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static stubidp.saml.hub.validators.response.common.ResponseSizeValidator.LOWER_BOUND;
import static stubidp.saml.hub.validators.response.common.ResponseSizeValidator.UPPER_BOUND;

class ResponseSizeValidatorTest {

    @Test
    void shouldThrowNullPointerExceptionWhenInputNull() {
        assertThrows(NullPointerException.class, () -> new ResponseSizeValidator().validate(null));
    }

    @Test
    void shouldThrowSamlTransformationErrorExceptionWhenInputTooSmall() {
        assertThrows(SamlTransformationErrorException.class, 
                () -> new ResponseSizeValidator().validate(createString(LOWER_BOUND - 1)));
    }

    @Test
    void shouldThrowSamlTransformationErrorExceptionWhenInputTooLarge() {
        assertThrows(SamlTransformationErrorException.class,
                () -> new ResponseSizeValidator().validate(createString(UPPER_BOUND + 1)));
    }

    @Test
    void shouldThrowNoExceptionWhenResponseSizeOnBoundry() {
        new ResponseSizeValidator().validate(createString(LOWER_BOUND));
        new ResponseSizeValidator().validate(createString(UPPER_BOUND));
    }

    private String createString(int length) {
        char[] charArray = new char[length];
        Arrays.fill(charArray, 'a');
        return new String(charArray);
    }

} 