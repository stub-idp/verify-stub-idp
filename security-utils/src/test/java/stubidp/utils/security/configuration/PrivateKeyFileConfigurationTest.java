package stubidp.utils.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivateKeyFileConfigurationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_loadPrivateKeyFromJSON() throws Exception {
        String path = getClass().getClassLoader().getResource("private_key.pk8").getPath();
        PrivateKeyConfiguration privateKeyFileConfiguration = objectMapper.readValue("{\"type\": \"file\", \"keyFile\": \"" + path + "\"}", PrivateKeyConfiguration.class);

        assertThat(privateKeyFileConfiguration.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    public void should_loadPrivateKeyWhenUsingAliases() throws Exception {
        String path = getClass().getClassLoader().getResource("private_key.pk8").getPath();
        List<String> aliases = Arrays.asList("key", "keyFile");

        for (String alias : aliases) {
            PrivateKeyConfiguration privateKeyFileConfiguration = objectMapper.readValue("{\"type\": \"file\", \"" + alias + "\": \"" + path + "\"}", PrivateKeyConfiguration.class);
            assertThat(privateKeyFileConfiguration.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
        }
    }

    @Test
    public void should_ThrowExceptionWhenFileDoesNotExist() throws Exception {
        final InvalidDefinitionException exception = Assertions.assertThrows(InvalidDefinitionException.class, () -> objectMapper.readValue("{\"keyFile\": \"/foo/bar\"}", PrivateKeyConfiguration.class));
        assertThat(exception.getMessage()).contains("NoSuchFileException");
    }

    @Test
    public void should_ThrowExceptionWhenFileDoesNotContainAPrivateKey() throws Exception {
        String path = getClass().getClassLoader().getResource("empty_file").getPath();
        final InvalidDefinitionException exception = Assertions.assertThrows(InvalidDefinitionException.class, () -> objectMapper.readValue("{\"keyFile\": \"" + path + "\"}", PrivateKeyConfiguration.class));
        assertThat(exception.getMessage()).contains("InvalidKeySpecException");
    }

    @Test
    public void should_throwAnExceptionWhenIncorrectJSONKeySpecified() throws Exception {
        String path = getClass().getClassLoader().getResource("empty_file").getPath();
        Assertions.assertThrows(InvalidDefinitionException.class, () -> objectMapper.readValue("{\"privateKeyFoo\": \"" + path + "\"}", PrivateKeyConfiguration.class));
    }
}
