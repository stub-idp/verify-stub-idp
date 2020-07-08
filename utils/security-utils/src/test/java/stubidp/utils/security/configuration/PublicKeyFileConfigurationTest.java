package stubidp.utils.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PublicKeyFileConfigurationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_loadPublicKeyFromJSON() throws Exception {
        String path = getClass().getClassLoader().getResource("public_key.crt").getPath();
        DeserializablePublicKeyConfiguration publicKeyConfiguration = objectMapper.readValue("{\"type\": \"file\", \"cert\": \"" + path + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class);

        assertThat(publicKeyConfiguration.getPublicKey().getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    public void should_loadPublicKeyWhenUsingAliases() throws Exception {
        String path = getClass().getClassLoader().getResource("public_key.crt").getPath();
        List<String> aliases = Arrays.asList("cert", "certFile");

        for (String alias : aliases) {
            DeserializablePublicKeyConfiguration publicKeyConfiguration = objectMapper.readValue(
                    "{\"type\": \"file\", \"" + alias + "\": \"" + path + "\", \"name\": \"someId\"}",
                    DeserializablePublicKeyConfiguration.class);

            assertThat(publicKeyConfiguration.getPublicKey().getAlgorithm()).isEqualTo("RSA");
        }
    }

    @Test
    public void should_ThrowExceptionWhenFileDoesNotExist() throws Exception {
        final ValueInstantiationException exception = Assertions.assertThrows(ValueInstantiationException.class, () -> objectMapper.readValue("{\"type\": \"file\", \"cert\": \"/foo/bar\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class));
        assertThat(exception.getMessage()).contains("NoSuchFileException");
    }

    @Test
    public void should_ThrowExceptionWhenFileDoesNotContainAPublicKey() throws Exception {
        String path = getClass().getClassLoader().getResource("empty_file").getPath();
        final ValueInstantiationException exception = Assertions.assertThrows(ValueInstantiationException.class, () -> objectMapper.readValue("{\"type\": \"file\", \"cert\": \"" + path + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class));
        assertThat(exception.getMessage()).contains("Unable to load certificate");
    }

    @Test
    public void should_ThrowExceptionWhenIncorrectKeySpecified() throws Exception {
        String path = getClass().getClassLoader().getResource("empty_file").getPath();
        Assertions.assertThrows(ValueInstantiationException.class, () -> objectMapper.readValue("{\"type\": \"file\", \"certFoo\": \"" + path + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class));
    }
}
