package stubidp.saml.serializers.deserializers.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static stubidp.saml.serializers.errors.SamlTransformationErrorFactory.invalidBase64Encoding;

public class Base64StringDecoderTest {

    private Base64StringDecoder samlStringProcessor;

    @BeforeEach
    public void setup() {
        samlStringProcessor = new Base64StringDecoder();
    }

    @Test
    public void shouldPassDecodedStringToNextProcessor() {
        assertThat(samlStringProcessor.decode(toBase64Encoded("string"))).isEqualTo("string");
    }

    private String toBase64Encoded(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes());
    }

    @Test
    public void shouldPassDecodedMultiLineStringToNextProcessor() {
        assertThat(samlStringProcessor.decode(toBase64Encoded("string") + "\n" + toBase64Encoded("string")))
                .isEqualTo("string" + "string");
    }

    @Test
    public void shouldHandleNotBase64Encoded() {
        final String input = "<SAMLRequest>&lt;&gt;</SAMLRequest>";

        try {
            samlStringProcessor.decode(input);
            fail("Expected action to throw");
        } catch (SamlTransformationErrorException e) {
            SamlValidationSpecificationFailure failure = invalidBase64Encoding(input);
            assertThat(e.getMessage()).isEqualTo(failure.getErrorMessage());
            assertThat(e.getLogLevel()).isEqualTo(failure.getLogLevel());
        }
    }
}
