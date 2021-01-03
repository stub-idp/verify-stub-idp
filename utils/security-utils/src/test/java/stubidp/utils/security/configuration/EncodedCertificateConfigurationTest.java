package stubidp.utils.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodedCertificateConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_loadPublicKeyFromJSON() throws Exception {
        String encodedCert = getCertificateAsString();
        String jsonConfig = "{\"type\": \"encoded\", \"cert\": \"" + encodedCert + "\", \"name\": \"someId\"}";
        DeserializablePublicKeyConfiguration config = objectMapper.readValue(jsonConfig, DeserializablePublicKeyConfiguration.class);

        assertThat(config.getPublicKey().getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    void should_ThrowExceptionWhenStringDoesNotContainAPublicKey() throws Exception {
        InputStream stream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("private_key.pk8"));
        String encodedKey = Base64.getEncoder().encodeToString(stream.readAllBytes());
        final ValueInstantiationException exception = Assertions.assertThrows(ValueInstantiationException.class,
                () -> objectMapper.readValue("{\"type\": \"encoded\", \"cert\": \"" + encodedKey + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class));
        assertThat(exception.getMessage()).contains("Unable to load certificate");
    }

    @Test
    void should_ThrowExceptionWhenStringIsNotBase64Encoded() {
        Assertions.assertThrows(ValueInstantiationException.class,
                () -> objectMapper.readValue("{\"type\": \"encoded\", \"cert\": \"" + "FOOBARBAZ" + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class));
    }

    @Test
    void should_ThrowExceptionWhenIncorrectKeySpecified() {
        String jsonConfig = "{\"type\": \"encoded\", \"certFoo\": \"" + "empty_file" + "\", \"name\": \"someId\"}";
        Assertions.assertThrows(ValueInstantiationException.class,
                () -> objectMapper.readValue(jsonConfig, DeserializablePublicKeyConfiguration.class));
    }

    private String getCertificateAsString() throws IOException {
        InputStream stream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("public_key.crt"));
        byte[] cert = stream.readAllBytes();
        return Base64.getEncoder().encodeToString(cert);
    }

}