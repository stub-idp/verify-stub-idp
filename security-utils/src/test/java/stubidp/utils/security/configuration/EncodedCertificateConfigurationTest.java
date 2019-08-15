package stubidp.utils.security.configuration;

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

public class EncodedCertificateConfigurationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_loadPublicKeyFromJSON() throws Exception {
        String encodedCert = getCertificateAsString();
        String jsonConfig = "{\"type\": \"encoded\", \"cert\": \"" + encodedCert + "\", \"name\": \"someId\"}";
        DeserializablePublicKeyConfiguration config = objectMapper.readValue(jsonConfig, DeserializablePublicKeyConfiguration.class);

        assertThat(config.getPublicKey().getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    public void should_ThrowExceptionWhenStringDoesNotContainAPublicKey() throws Exception {
        String path = Resources.getResource("private_key.pk8").getFile();
        byte[] key = Files.readAllBytes(new File(path).toPath());
        String encodedKey = Base64.getEncoder().encodeToString(key);
        final InvalidDefinitionException exception = Assertions.assertThrows(InvalidDefinitionException.class,
                () -> objectMapper.readValue("{\"type\": \"encoded\", \"cert\": \"" + encodedKey + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class));
        assertThat(exception.getMessage()).contains("Unable to load certificate");
    }

    @Test
    public void should_ThrowExceptionWhenStringIsNotBase64Encoded() throws Exception {
        Assertions.assertThrows(InvalidDefinitionException.class,
                () -> objectMapper.readValue("{\"type\": \"encoded\", \"cert\": \"" + "FOOBARBAZ" + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class));
    }

    @Test
    public void should_ThrowExceptionWhenIncorrectKeySpecified() throws Exception {
        String path = getClass().getClassLoader().getResource("empty_file").getPath();
        String jsonConfig = "{\"type\": \"encoded\", \"certFoo\": \"" + path + "\", \"name\": \"someId\"}";
        Assertions.assertThrows(InvalidDefinitionException.class,
                () -> objectMapper.readValue(jsonConfig, DeserializablePublicKeyConfiguration.class));
    }

    private String getCertificateAsString() throws IOException {
        String path = Resources.getResource("public_key.crt").getFile();
        byte[] cert = Files.readAllBytes(new File(path).toPath());
        return Base64.getEncoder().encodeToString(cert);
    }

}