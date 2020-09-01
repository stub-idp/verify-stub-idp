package stubidp.utils.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivateKeyConfigurationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldDefaultToFileType() throws Exception {
        String path = getClass().getClassLoader().getResource("private_key.pk8").getPath();
        PrivateKeyConfiguration config = objectMapper.readValue("{\"key\": \"" + path + "\"}", PrivateKeyConfiguration.class);
        assertThat(config.getClass()).isEqualTo(PrivateKeyFileConfiguration.class);
    }

    @Test
    public void shouldUseFileTypeWhenSpecified() throws Exception {
        String path = getClass().getClassLoader().getResource("private_key.pk8").getPath();
        PrivateKeyConfiguration config = objectMapper.readValue("{\"type\": \"file\", \"key\": \"" + path + "\"}", PrivateKeyConfiguration.class);
        assertThat(config.getClass()).isEqualTo(PrivateKeyFileConfiguration.class);
    }

    @Test
    public void shouldUseEncodedTypeWhenSpecified() throws Exception {
        PrivateKeyConfiguration config = objectMapper.readValue("{\"type\": \"encoded\", \"key\": \"" + getKeyAsBase64() + "\"}", PrivateKeyConfiguration.class);
        assertThat(config.getClass()).isEqualTo(EncodedPrivateKeyConfiguration.class);
    }

    private String getKeyAsBase64() throws IOException {
        InputStream stream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("private_key.pk8"));
        return Base64.getEncoder().encodeToString(stream.readAllBytes());
    }
}