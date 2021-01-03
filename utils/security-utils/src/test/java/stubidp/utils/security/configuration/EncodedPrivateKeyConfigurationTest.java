package stubidp.utils.security.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodedPrivateKeyConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_loadPrivateKeyFromJSON() throws Exception {
        String jsonConfig = "{\"type\": \"encoded\", \"key\": \"" + getKeyAsBase64() + "\"}";
        PrivateKeyConfiguration configuration = objectMapper.readValue(jsonConfig, PrivateKeyConfiguration.class);
        assertThat(configuration.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    void should_ThrowExceptionWhenKeyIsNotBase64() {
        Assertions.assertThrows(ValueInstantiationException.class, () -> objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false).readValue("{\"type\": \"encoded\", \"key\": \"not-a-key\"}", PrivateKeyConfiguration.class));
    }

    @Test
    void should_ThrowExceptionWhenKeyIsNotAValidKey() {
        final ValueInstantiationException exception = Assertions.assertThrows(ValueInstantiationException.class, () -> objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false).readValue("{\"type\": \"encoded\", \"key\": \"dGVzdAo=\"}", PrivateKeyConfiguration.class));
        assertThat(exception.getMessage()).contains("InvalidKeySpecException");
    }

    @Test
    void should_throwAnExceptionWhenIncorrectFieldSpecified() {
        Assertions.assertThrows(ValueInstantiationException.class, () -> objectMapper.readValue("{\"privateKeyFoo\": \"" + "foobar" + "\"}", PrivateKeyConfiguration.class));
    }

    private String getKeyAsBase64() throws IOException {
        InputStream stream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("private_key.pk8"));
        return Base64.getEncoder().encodeToString(stream.readAllBytes());
    }
}