package stubidp.utils.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.utils.security.security.Certificate.BEGIN_CERT;
import static stubidp.utils.security.security.Certificate.END_CERT;

public class X509CertificateConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_loadPublicKey() throws Exception {
        String strippedCert = getCertificateString();
        String jsonConfig = "{\"type\": \"x509\", \"cert\": \"" + strippedCert + "\", \"name\": \"someId\"}";
        DeserializablePublicKeyConfiguration config = objectMapper.readValue(jsonConfig, DeserializablePublicKeyConfiguration.class);

        assertThat(config.getPublicKey().getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    void should_loadPublicKeyWhenUsingAliases() throws Exception {
        String strippedCert = getCertificateString();
        List<String> aliases = Arrays.asList("cert", "x509");

        for (String alias : aliases) {
            DeserializablePublicKeyConfiguration publicKeyConfiguration = objectMapper.readValue(
                    "{\"type\": \"x509\", \"" + alias + "\": \"" + strippedCert + "\", \"name\": \"someId\"}",
                    DeserializablePublicKeyConfiguration.class);

            assertThat(publicKeyConfiguration.getPublicKey().getAlgorithm()).isEqualTo("RSA");
        }
    }

    @Test
    void should_ThrowExceptionWhenStringDoesNotContainAPublicKey() {
        String encodedKey = Base64.getEncoder().encodeToString("not-a-fullCertificate".getBytes());
        final ValueInstantiationException exception = Assertions.assertThrows(ValueInstantiationException.class, () -> objectMapper.readValue("{\"type\": \"x509\", \"cert\": \"" + encodedKey + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class));
        assertThat(exception.getMessage()).contains("Unable to load certificate");
    }

    @Test
    void should_ThrowExceptionWhenIncorrectKeySpecified() {
        String path = getClass().getClassLoader().getResource("empty_file").getPath();
        String jsonConfig = "{\"type\": \"x509\", \"certFoo\": \"" + path + "\", \"name\": \"someId\"}";
        Assertions.assertThrows(ValueInstantiationException.class, () -> objectMapper.readValue(jsonConfig, DeserializablePublicKeyConfiguration.class));
    }

    private String getCertificateString() throws IOException {
        InputStream stream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("public_key.crt"));
        String fullCert = new String(stream.readAllBytes());
        return fullCert
                .replace(BEGIN_CERT, "")
                .replace(END_CERT, "")
                .replace("\n", "")
                .trim();
    }

}