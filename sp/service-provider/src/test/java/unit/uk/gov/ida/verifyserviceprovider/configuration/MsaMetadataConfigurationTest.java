package unit.uk.gov.ida.verifyserviceprovider.configuration;

import org.junit.jupiter.api.Test;
import uk.gov.ida.verifyserviceprovider.configuration.MsaMetadataConfiguration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.MSA_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class MsaMetadataConfigurationTest {

    @Test
    void shouldSetDefaultConfigValuesWhenNotProvided() throws Exception {
        String configurationAsString = "{\"uri\": \"http://some-msa-uri\", \"expectedEntityId\": \"foo\"}";

        MsaMetadataConfiguration actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MsaMetadataConfiguration.class);

        assertThat(actualConfiguration.getMinRefreshDelay()).isEqualTo(Duration.ofMillis(60000));
        assertThat(actualConfiguration.getMaxRefreshDelay()).isEqualTo(Duration.ofMillis(600000));
        assertThat(actualConfiguration.getJerseyClientConfiguration()).isNotNull();
        assertThat(actualConfiguration.getJerseyClientName()).isEqualTo(MSA_JERSEY_CLIENT_NAME);
    }

    @Test
    void shouldNotAllowEmptyExpectedEntityId() {
        String configurationAsString = "{\"uri\": \"http://some-msa-uri\"}";

        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> OBJECT_MAPPER.readValue(configurationAsString, MsaMetadataConfiguration.class))
                .withMessageContaining("Missing required creator property 'expectedEntityId'");
    }
}