package unit.uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import common.uk.gov.ida.verifyserviceprovider.utils.EnvironmentHelper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.testing.ResourceHelpers;
import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ida.verifyserviceprovider.configuration.EuropeanIdentityConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyHubConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.exceptions.NoHashingEntityIdIsProvidedError;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static io.dropwizard.jersey.validation.Validators.newValidator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;

@ExtendWith(MockitoExtension.class)
public class VerifyServiceProviderConfigurationTest {

    private final YamlConfigurationFactory<VerifyServiceProviderConfiguration> factory = new YamlConfigurationFactory<>(
            VerifyServiceProviderConfiguration.class,
            newValidator(),
            newObjectMapper(),
            "dw."
    );
    private final EnvironmentHelper environmentHelper = new EnvironmentHelper();

    @Test
    @Disabled("cannot setEnv on java16+")
    void shouldNotComplainWhenConfiguredCorrectly() throws Exception {
        environmentHelper.setEnv(new HashMap<>() {{
            put("PORT", "50555");
            put("LOG_LEVEL", "ERROR");
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("MSA_METADATA_URL", "some-msa-metadata-url");
            put("MSA_ENTITY_ID", "some-msa-entity-id");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]");
            put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("SAML_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("CLOCK_SKEW", "PT30s");
            put("EUROPEAN_IDENTITY_ENABLED", "false");
            put("HUB_CONNECTOR_ENTITY_ID", "etc");
            put("TRUST_ANCHOR_URI", "etc");
            put("METADATA_SOURCE_URI", "etc");
            put("TRUSTSTORE_PATH", "etc");
            put("TRUSTSTORE_PASSWORD", "etc");
        }});

        factory.build(
                new SubstitutingSourceProvider(
                        new FileConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                ),
                ResourceHelpers.resourceFilePath("verify-service-provider-with-msa.yml")
        );
        environmentHelper.cleanEnv();
    }

    @Test
    void shouldReturnHashingEntityIdWhenItIsDefined() {

        VerifyServiceProviderConfiguration verifyServiceProviderConfiguration = aVerifyServiceProviderConfiguration(
                Arrays.asList("http://some-service-entity-id","http://some-service-entity-id2"),
                "provided-hashing-entity-id"
        );

        assertThat(verifyServiceProviderConfiguration.getHashingEntityId()).isEqualTo("provided-hashing-entity-id");
    }


    @Test
    void shouldUseServiceEntityIdForHashingWhenHashingEntityIdNotSpecified(){

        VerifyServiceProviderConfiguration verifyServiceProviderConfiguration = aVerifyServiceProviderConfiguration(
                Collections.singletonList("http://some-service-entity-id"),
                null
        );

        assertThat(verifyServiceProviderConfiguration.getHashingEntityId()).isEqualTo("http://some-service-entity-id");
    }

    @Test
    void shouldReturnHashingEntityIdWhenOneServiceEntityIdIsProvided() {

        VerifyServiceProviderConfiguration verifyServiceProviderConfiguration = aVerifyServiceProviderConfiguration(
                Collections.singletonList("http://some-service-entity-id"),
                "provided-hashing-entity-id"
        );

        assertThat(verifyServiceProviderConfiguration.getHashingEntityId()).isEqualTo("provided-hashing-entity-id");
    }

    @Test
    void shouldThrowNoHashingEntityIdIsProvidedErrorWhenMultipleServiceEntityIdsAreProvided() {

        VerifyServiceProviderConfiguration verifyServiceProviderConfiguration = aVerifyServiceProviderConfiguration(
                Arrays.asList("http://some-service-entity-id", "http://some-service-entity-id2"),
                null
        );

        assertThatExceptionOfType(NoHashingEntityIdIsProvidedError.class)
                .isThrownBy(verifyServiceProviderConfiguration::getHashingEntityId)
                .withMessage("No HashingEntityId is provided");
    }

    @Test
    void shouldNotAllowMsaAndEidasConfigTogether() {
        Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("PORT", "50555")
                .put("LOG_LEVEL", "ERROR")
                .put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL")
                .put("MSA_METADATA_URL", "some-msa-metadata-url")
                .put("MSA_ENTITY_ID", "some-msa-entity-id")
                .put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]")
                .put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY)
                .put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY)
                .put("SAML_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY)
                .put("CLOCK_SKEW", "PT30s")
                .put("EUROPEAN_IDENTITY_ENABLED", "false")
                .put("HUB_CONNECTOR_ENTITY_ID", "etc")
                .put("TRUST_ANCHOR_URI", "etc")
                .put("METADATA_SOURCE_URI", "etc")
                .put("TRUSTSTORE_PATH", "etc")
                .put("TRUSTSTORE_PASSWORD", "etc")
                .build();

        String newErrorMessage = "eIDAS and MSA support cannot be set together." +
                " The VSP's eIDAS support is only available when it operates without the MSA";
        assertThatThrownBy(() -> factory.build(
                new SubstitutingSourceProvider(
                        new FileConfigurationSourceProvider(),
                        new StringSubstitutor(map)
                ),
                ResourceHelpers.resourceFilePath("verify-service-provider-with-msa-and-eidas.yml")
        )).isInstanceOf(ConfigurationException.class).hasMessageContaining(newErrorMessage);
    }

    private VerifyServiceProviderConfiguration aVerifyServiceProviderConfiguration(List<String> serviceEntityIds, String hashingEntityId) {
        return new VerifyServiceProviderConfiguration(
                serviceEntityIds,
                hashingEntityId,
                mock(VerifyHubConfiguration.class),
                mock(PrivateKey.class),
                mock(PrivateKey.class),
                mock(PrivateKey.class),
                Optional.empty(),
                Duration.ofMillis(1000L),
                Optional.ofNullable(mock(EuropeanIdentityConfiguration.class))
        );
    }


    @Test
    void shouldNotAllowNullValues() {
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> factory.build(new StringConfigurationSourceProvider("server: "), ""))
                .withMessageContaining("server must not be null");
    }

    @Test
    void shouldNotAllowEmptySamlSigningKey() {
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> factory.build(new StringConfigurationSourceProvider("samlSigningKey: \"\""), ""))
                .withMessageContaining("Failed to parse configuration at: samlSigningKey");
    }

    @Test
    void shouldNotAllowEmptySamlPrimaryEncryptionKey() {
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> factory.build(new StringConfigurationSourceProvider("samlPrimaryEncryptionKey: \"\""), ""))
                .withMessageContaining("Failed to parse configuration at: samlPrimaryEncryptionKey");
    }

    private static class StringConfigurationSourceProvider implements ConfigurationSourceProvider {
        private final String configuration;

        public StringConfigurationSourceProvider(String configuration) {
            this.configuration = configuration;
        }

        @Override
        public InputStream open(String path) {
            return new ByteArrayInputStream(this.configuration.getBytes());
        }
    }
}
