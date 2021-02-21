package acceptance.uk.gov.ida.verifyserviceprovider.rules;

import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.test.utils.keystore.KeyStoreResource;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import static stubidp.saml.test.builders.CertificateBuilder.aCertificate;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;

public class VerifyServiceProviderAppExtension extends DropwizardAppExtension<VerifyServiceProviderConfiguration> {

    private static final KeyStoreResource KEY_STORE_RESOURCE = aKeyStoreResource()
        .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
        .build();
    static {
        KEY_STORE_RESOURCE.create();
    }

    public VerifyServiceProviderAppExtension(String secondaryEncryptionKey, String serviceEntityIdOverride) {
        super(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            ConfigOverride.config("serviceEntityIds", serviceEntityIdOverride),
            ConfigOverride.config("hashingEntityId", "some-hashing-entity-id"),
            ConfigOverride.config("server.connector.port", String.valueOf(0)),
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
            ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("samlSecondaryEncryptionKey", secondaryEncryptionKey),
            ConfigOverride.config("europeanIdentity.enabled", "false"),
            ConfigOverride.config("europeanIdentity.hubConnectorEntityId", "dummyEntity"),
            ConfigOverride.config("europeanIdentity.trustAnchorUri", "http://dummy.com"),
            ConfigOverride.config("europeanIdentity.metadataSourceUri", "http://dummy.com"),
            ConfigOverride.config("europeanIdentity.trustStore.path", KEY_STORE_RESOURCE.getAbsolutePath()),
            ConfigOverride.config("europeanIdentity.trustStore.password", KEY_STORE_RESOURCE.getPassword())
        );
    }

    public VerifyServiceProviderAppExtension(boolean isEidasEnabled, MockMsaServer msaServer, String secondaryEncryptionKey, String serviceEntityIdOverride) {
        super(
            VerifyServiceProviderApplication.class,
            ResourceHelpers.resourceFilePath("verify-service-provider-with-msa.yml"),
            ConfigOverride.config("serviceEntityIds", serviceEntityIdOverride),
            ConfigOverride.config("hashingEntityId", "some-hashing-entity-id"),
            ConfigOverride.config("server.connector.port", String.valueOf(0)),
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
            ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("samlSecondaryEncryptionKey", secondaryEncryptionKey),
            ConfigOverride.config("msaMetadata.uri", () -> {
                IdaSamlBootstrap.bootstrap();
                msaServer.serveDefaultMetadata();
                msaServer.start();
                return msaServer.getUri();
            }),
            ConfigOverride.config("msaMetadata.expectedEntityId", MockMsaServer.MSA_ENTITY_ID)
        );
    }

    public VerifyServiceProviderAppExtension() {
        this(TEST_RP_PRIVATE_ENCRYPTION_KEY, "http://verify-service-provider");
    }

    public VerifyServiceProviderAppExtension(boolean isEidasEnabled, MockMsaServer msaServer) {
        this(isEidasEnabled, msaServer, TEST_RP_PRIVATE_ENCRYPTION_KEY, "http://verify-service-provider");
    }

    public VerifyServiceProviderAppExtension(MockMsaServer msaServer, String secondaryEncryptionKey, String serviceEntityIdOverride) {
        this(false, msaServer, secondaryEncryptionKey, serviceEntityIdOverride);
    }

    public VerifyServiceProviderAppExtension(MockMsaServer msaServer) {
        this(msaServer,TEST_RP_PRIVATE_ENCRYPTION_KEY, "http://verify-service-provider");
    }

    public VerifyServiceProviderAppExtension(MockMsaServer msaServer, String serviceEntityIdOverride) {
        this(msaServer,TEST_RP_PRIVATE_ENCRYPTION_KEY, serviceEntityIdOverride);
    }
}
