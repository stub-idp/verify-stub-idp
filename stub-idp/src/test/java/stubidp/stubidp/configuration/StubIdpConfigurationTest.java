package stubidp.stubidp.configuration;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static io.dropwizard.jersey.validation.Validators.newValidator;
import static org.assertj.core.api.Assertions.assertThat;

public class StubIdpConfigurationTest {

    private final YamlConfigurationFactory<StubIdpConfiguration> factory = new YamlConfigurationFactory<>(
        StubIdpConfiguration.class, newValidator(), newObjectMapper(), "dw.");

    @Test
    public void shouldNotAllowNullValues() throws Exception {
        final Exception e = Assertions.assertThrows(Exception.class, () -> factory.build(new StringConfigurationSourceProvider("url: "), ""));

        assertThat(e.getMessage()).contains("assertionLifetime must not be null");
        assertThat(e.getMessage()).contains("databaseConfiguration must not be null");
        assertThat(e.getMessage()).contains("europeanIdentity must not be null");
        assertThat(e.getMessage()).contains("metadata must not be null");
        assertThat(e.getMessage()).contains("saml must not be null");
        assertThat(e.getMessage()).contains("serviceInfo must not be null");
        assertThat(e.getMessage()).contains("signingKeyPairConfiguration must not be null");
        assertThat(e.getMessage()).contains("stubIdpYmlFileRefresh must not be null");
        assertThat(e.getMessage()).contains("stubIdpsYmlFileLocation must not be null");
    }

    static class StringConfigurationSourceProvider implements ConfigurationSourceProvider {
        private String configuration;

        public StringConfigurationSourceProvider(String configuration) {
            this.configuration = configuration;
        }

        @Override
        public InputStream open(String path) throws IOException {
            return new ByteArrayInputStream(this.configuration.getBytes());
        }
    }

}
