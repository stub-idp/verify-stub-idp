package stubidp.saml.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import stubidp.test.utils.keystore.KeyStoreRule;
import stubidp.test.utils.keystore.builders.KeyStoreRuleBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;

public class TrustStoreConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RegisterExtension
    public static final KeyStoreRule keyStoreRule = KeyStoreRuleBuilder.aKeyStoreRule().withCertificate("hub", HUB_TEST_PUBLIC_SIGNING_CERT).build();

    @Test
    public void shouldLoadTrustStoreFromFile() throws IOException {
        String jsonConfig = "{\"type\": \"file\", \"trustStorePath\": " + objectMapper.writeValueAsString(keyStoreRule.getAbsolutePath()) + ", \"trustStorePassword\": \"" + keyStoreRule.getPassword() + "\"}";
        TrustStoreConfiguration config = objectMapper.readValue(jsonConfig, TrustStoreConfiguration.class);

        assertThat(config.getTrustStore()).isNotNull();
    }

    @Test
    public void shouldLoadTrustStoreFromEncodedString() throws IOException {
        byte[] trustStore = Files.readAllBytes(new File(keyStoreRule.getAbsolutePath()).toPath());
        String encodedTrustStore = Base64.getEncoder().encodeToString(trustStore);
        String jsonConfig = "{\"type\": \"encoded\", \"store\": \"" + encodedTrustStore + "\", \"trustStorePassword\": \"" + keyStoreRule.getPassword() + "\"}";
        TrustStoreConfiguration config = objectMapper.readValue(jsonConfig, TrustStoreConfiguration.class);

        assertThat(config.getTrustStore()).isNotNull();
    }

    @Test
    public void shouldDefaultToFileBackedWhenNoTypeProvided() throws IOException {
        String jsonConfig = "{\"trustStorePath\": " + objectMapper.writeValueAsString(keyStoreRule.getAbsolutePath()) + ", \"trustStorePassword\": \"" + keyStoreRule.getPassword() + "\"}";
        TrustStoreConfiguration config = objectMapper.readValue(jsonConfig, TrustStoreConfiguration.class);

        assertThat(config.getTrustStore()).isNotNull();
    }

    @Test
    public void shouldLoadTrustStoreFromFileUsingAliases() throws IOException {
        String jsonConfig = "{\"path\": " + objectMapper.writeValueAsString(keyStoreRule.getAbsolutePath()) + ", \"password\": \"" + keyStoreRule.getPassword() + "\"}";
        TrustStoreConfiguration config = objectMapper.readValue(jsonConfig, TrustStoreConfiguration.class);

        assertThat(config.getTrustStore()).isNotNull();
    }

    @Test
    public void shouldThrowExceptionWhenIncorrectKeySpecified() {
        String jsonConfig = "{\"type\": \"file\", \"trustStorePathhhh\": \"path\", \"trustStorePassword\": \"puppet\"}";
        Assertions.assertThrows(UnrecognizedPropertyException.class, () -> objectMapper.readValue(jsonConfig, TrustStoreConfiguration.class));
    }
}
