package stubidp.saml.serializers.deserializers.validators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.serializers.errors.SamlTransformationErrorFactory.invalidBase64Encoding;

class Base64StringDecoderTest {

    private final Base64StringDecoder samlStringProcessor = new Base64StringDecoder();

    @Test
    void shouldPassDecodedStringToNextProcessor() {
        assertThat(samlStringProcessor.decode(toBase64Encoded("string"))).isEqualTo("string");
    }

    private String toBase64Encoded(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes());
    }

    @Test
    void shouldPassDecodedMultiLineStringToNextProcessor() {
        assertThat(samlStringProcessor.decode(toBase64Encoded("string") + "\n" + toBase64Encoded("string")))
                .isEqualTo("string" + "string");
    }

    @Test
    void shouldHandleNotBase64Encoded() {
        final String input = "<SAMLRequest>&lt;&gt;</SAMLRequest>";
        final SamlValidationSpecificationFailure expectedFailure = invalidBase64Encoding(input);

        final SamlTransformationErrorException exceptionThrown = Assertions.assertThrows(SamlTransformationErrorException.class,
                () -> samlStringProcessor.decode(input));

        assertThat(exceptionThrown.getMessage()).isEqualTo(expectedFailure.getErrorMessage());
        assertThat(exceptionThrown.getLogLevel()).isEqualTo(expectedFailure.getLogLevel());
    }
}
