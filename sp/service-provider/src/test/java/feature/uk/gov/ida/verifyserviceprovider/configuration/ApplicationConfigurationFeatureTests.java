package feature.uk.gov.ida.verifyserviceprovider.configuration;

import common.uk.gov.ida.verifyserviceprovider.utils.EnvironmentHelper;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.HubEnvironment;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.time.Duration;
import java.util.HashMap;

import static net.shibboleth.utilities.java.support.codec.Base64Support.decode;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;

@ExtendWith(MockitoExtension.class)
@Disabled("cannot setEnv on java16+")
public class ApplicationConfigurationFeatureTests {

    private final EnvironmentHelper environmentHelper = new EnvironmentHelper();

    private DropwizardAppExtension<VerifyServiceProviderConfiguration> application;

    @AfterEach
    public void cleanup() {
        application.getTestSupport().after();
        environmentHelper.cleanEnv();
    }

    @Test
    public void applicationShouldStartUp() throws Exception {
        environmentHelper.setEnv(new HashMap<>() {{
            put("PORT", "50555");
            put("LOG_LEVEL", "ERROR");
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]");
            put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("SAML_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("CLOCK_SKEW", "PT30s");
        }});

        application = new DropwizardAppExtension<>(
                VerifyServiceProviderApplication.class,
                "verify-service-provider.yml",
                ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG")
        );

        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(application.getLocalPort()).isEqualTo(50555);
        assertThat(((DefaultLoggingFactory) configuration.getLoggingFactory()).getLevel()).isEqualTo("ERROR");
        assertThat(configuration.getHubSsoLocation().toString()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getSsoLocation().toString());
        assertThat(configuration.getVerifyHubMetadata().getUri().toString()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getMetadataUri().toString());
        assertThat(configuration.getVerifyHubMetadata().getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
        assertThat(configuration.getServiceEntityIds()).containsExactly("http://some-service-entity-id");
        assertThat(configuration.getSamlSigningKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_SIGNING_KEY));
        assertThat(configuration.getSamlPrimaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getSamlSecondaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getClockSkew()).isEqualTo(Duration.ofSeconds(30));
        assertThat(configuration.getEuropeanIdentity().isPresent()).isTrue();
        assertThat(configuration.getEuropeanIdentity().get().getAllAcceptableHubConnectorEntityIds()).containsAll(HubEnvironment.COMPLIANCE_TOOL.getEidasDefaultAcceptableHubConnectorEntityIds());
        assertThat(configuration.getEuropeanIdentity().get().getMetadataSourceUri()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getEidasMetadataSourceUri());
        assertThat(configuration.getEuropeanIdentity().get().getTrustAnchorUri()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getEidasMetadataTrustAnchorUri());
        assertThat(configuration.getEuropeanIdentity().get().getTrustStore().getCertificate("idaca").toString())
                .isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getMetadataTrustStore().getCertificate("idaca").toString());
        assertThat(configuration.getEuropeanIdentity().get().getTrustStore().getCertificate("idametadata").toString())
                .isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getMetadataTrustStore().getCertificate("idametadata").toString());

    }

    @Test
    public void applicationShouldStartUpWithListOfServiceEntityIds() throws Exception {
        environmentHelper.setEnv(new HashMap<>() {{
            put("PORT", "50555");
            put("LOG_LEVEL", "ERROR");
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\",\"http://some-other-service-entity-id\"]");
            put("HASHING_ENTITY_ID", "some-hashing-entity-id");
            put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("SAML_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("CLOCK_SKEW", "PT30s");
        }});

        application = new DropwizardAppExtension<>(
                VerifyServiceProviderApplication.class,
                "verify-service-provider.yml",
                ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG")
        );

        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(application.getLocalPort()).isEqualTo(50555);
        assertThat(((DefaultLoggingFactory) configuration.getLoggingFactory()).getLevel()).isEqualTo("ERROR");
        assertThat(configuration.getHubSsoLocation().toString()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getSsoLocation().toString());
        assertThat(configuration.getVerifyHubMetadata().getUri().toString()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getMetadataUri().toString());
        assertThat(configuration.getVerifyHubMetadata().getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
        assertThat(configuration.getServiceEntityIds()).containsExactly("http://some-service-entity-id", "http://some-other-service-entity-id");
        assertThat(configuration.getHashingEntityId()).isEqualTo("some-hashing-entity-id");
        assertThat(configuration.getSamlSigningKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_SIGNING_KEY));
        assertThat(configuration.getSamlPrimaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getSamlSecondaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getClockSkew()).isEqualTo(Duration.ofSeconds(30));
    }
}
