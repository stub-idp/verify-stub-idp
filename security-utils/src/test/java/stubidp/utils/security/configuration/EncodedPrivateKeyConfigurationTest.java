package stubidp.utils.security.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodedPrivateKeyConfigurationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_loadPrivateKeyFromJSON() throws Exception {
        String jsonConfig = "{\"type\": \"encoded\", \"key\": \"" + getKeyAsBase64() + "\"}";
        PrivateKeyConfiguration configuration = objectMapper.readValue(jsonConfig, PrivateKeyConfiguration.class);
        assertThat(configuration.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    public void should_ThrowExceptionWhenKeyIsNotBase64() throws Exception {
        Assertions.assertThrows(InvalidDefinitionException.class, () -> objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false).readValue("{\"type\": \"encoded\", \"key\": \"not-a-key\"}", PrivateKeyConfiguration.class));
    }

    @Test
    public void should_ThrowExceptionWhenKeyIsNotAValidKey() throws Exception {
        final InvalidDefinitionException exception = Assertions.assertThrows(InvalidDefinitionException.class, () -> objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false).readValue("{\"type\": \"encoded\", \"key\": \"dGVzdAo=\"}", PrivateKeyConfiguration.class));
        assertThat(exception.getMessage()).contains("InvalidKeySpecException");
    }

    @Test
    public void should_throwAnExceptionWhenIncorrectFieldSpecified() throws Exception {
        Assertions.assertThrows(InvalidDefinitionException.class, () -> objectMapper.readValue("{\"privateKeyFoo\": \"" + "foobar" + "\"}", PrivateKeyConfiguration.class));
    }

    private String getKeyAsBase64() throws IOException {
        String path = Resources.getResource("private_key.pk8").getFile();
        byte[] key = Files.readAllBytes(new File(path).toPath());
        return Base64.getEncoder().encodeToString(key);
    }
}