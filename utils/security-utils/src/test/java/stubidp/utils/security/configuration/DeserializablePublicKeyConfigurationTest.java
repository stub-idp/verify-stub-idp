package stubidp.utils.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.utils.security.security.Certificate.BEGIN_CERT;
import static stubidp.utils.security.security.Certificate.END_CERT;

public class DeserializablePublicKeyConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String CERT_NAME = "public_key.crt";

    @Test
    public void shouldDefaultToFileType() throws Exception {
        DeserializablePublicKeyConfiguration publicKeyConfiguration = objectMapper.readValue("{\"cert\": \"" + getCertPath() + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class);
        assertThat(publicKeyConfiguration.getClass()).isEqualTo(PublicKeyFileConfiguration.class);
    }

    @Test
    public void shouldUseFileTypeWhenSpecified() throws Exception {
        DeserializablePublicKeyConfiguration publicKeyConfiguration = objectMapper.readValue("{\"type\": \"file\", \"cert\": \"" + getCertPath() + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class);
        assertThat(publicKeyConfiguration.getClass()).isEqualTo(PublicKeyFileConfiguration.class);
    }

    @Test
    public void shouldUseEncodedTypeWhenSpecified() throws Exception {
        DeserializablePublicKeyConfiguration publicKeyConfiguration = objectMapper.readValue("{\"type\": \"encoded\", \"cert\": \"" + getBase64Cert() + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class);
        assertThat(publicKeyConfiguration.getClass()).isEqualTo(EncodedCertificateConfiguration.class);
    }

    @Test
    public void shouldUseX509TypeWhenSpecified() throws Exception {
        DeserializablePublicKeyConfiguration publicKeyConfiguration = objectMapper.readValue("{\"type\": \"x509\", \"cert\": \"" + getStrippedCert() + "\", \"name\": \"someId\"}", DeserializablePublicKeyConfiguration.class);
        assertThat(publicKeyConfiguration.getClass()).isEqualTo(X509CertificateConfiguration.class);
    }

    private String getCertificateString() {
        try {
            InputStream stream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(CERT_NAME));
            return new String(stream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getCertPath() throws URISyntaxException {
        URL url = Objects.requireNonNull(getClass().getClassLoader().getResource(CERT_NAME));
        return Path.of(url.toURI());
    }

    private String getStrippedCert() {
        return getCertificateString()
                .replace(BEGIN_CERT, "")
                .replace(END_CERT, "")
                .replace("\n", "")
                .trim();
    }

    private String getBase64Cert() {
        return Base64.getEncoder().encodeToString(getCertificateString().getBytes());
    }

}