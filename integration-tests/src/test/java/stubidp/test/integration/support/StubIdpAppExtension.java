package stubidp.test.integration.support;

import certificates.values.CACertificates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import org.apache.commons.io.FileUtils;
import org.opensaml.core.config.InitializationService;
import stubidp.saml.metadata.test.factories.metadata.MetadataFactory;
import stubidp.saml.utils.Constants;
import stubidp.stubidp.StubIdpApplication;
import stubidp.stubidp.configuration.IdpStubsConfiguration;
import stubidp.stubidp.configuration.StubIdp;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.keystore.KeyStoreResource;
import stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;

public class StubIdpAppExtension extends DropwizardAppExtension<StubIdpConfiguration> {

    private static final String METADATA_PATH = "/uk/gov/ida/saml/metadata/sp";

    private static final HttpStubRule metadataServer = new HttpStubRule();
    private static final KeyStoreResource trustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final File STUB_IDPS_FILE = new File(System.getProperty("java.io.tmpdir"), "stub-idps.yml");

    private final List<StubIdp> stubIdps = new ArrayList<>();

    private static final HttpStubRule fakeFrontend = new HttpStubRule();

    public StubIdpAppExtension(ConfigOverride... configOverrides) {
        super(StubIdpApplication.class, "./configuration/stub-idp.yml", withDefaultOverrides(configOverrides));
        try {
            fakeFrontend.register("/get-available-services", 200, "application/json", "[]");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static ConfigOverride[] withDefaultOverrides(ConfigOverride ... configOverrides) {
        ImmutableList<ConfigOverride> overrides = ImmutableList.<ConfigOverride>builder()
                .add(ConfigOverride.config("metadata.uri", "http://localhost:" + metadataServer.getPort() + METADATA_PATH))
                .add(ConfigOverride.config("hubEntityId", HUB_ENTITY_ID))
                .add(ConfigOverride.config("basicAuthEnabledForUserResource", "true"))
                .add(ConfigOverride.config("server.requestLog.appenders[0].type", "console"))
                .add(ConfigOverride.config("server.applicationConnectors[0].port", "0"))
                .add(ConfigOverride.config("server.adminConnectors[0].port", "0"))
                .add(ConfigOverride.config("logging.appenders[0].type", "console"))
                .add(ConfigOverride.config("stubIdpsYmlFileLocation", STUB_IDPS_FILE.getAbsolutePath()))
                .add(ConfigOverride.config("metadata.trustStore.store", trustStore.getAbsolutePath()))
                .add(ConfigOverride.config("metadata.trustStore.password", trustStore.getPassword()))
                .add(ConfigOverride.config("europeanIdentity.enabled", "true"))
                .add(ConfigOverride.config("europeanIdentity.hubConnectorEntityId", HUB_ENTITY_ID))
                .add(ConfigOverride.config("europeanIdentity.stubCountryBaseUrl", "http://localhost:0"))
                .add(ConfigOverride.config("europeanIdentity.metadata.uri", "http://localhost:" + metadataServer.getPort() + METADATA_PATH))
                .add(ConfigOverride.config("europeanIdentity.metadata.expectedEntityId", HUB_ENTITY_ID))
                .add(ConfigOverride.config("europeanIdentity.metadata.trustStore.store", trustStore.getAbsolutePath()))
                .add(ConfigOverride.config("europeanIdentity.metadata.trustStore.password", trustStore.getPassword()))
                .add(ConfigOverride.config("signingKeyPairConfiguration.privateKeyConfiguration.type", "encoded"))
                .add(ConfigOverride.config("signingKeyPairConfiguration.privateKeyConfiguration.key", STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY))
                .add(ConfigOverride.config("signingKeyPairConfiguration.publicKeyConfiguration.type", "x509"))
                .add(ConfigOverride.config("signingKeyPairConfiguration.publicKeyConfiguration.cert", STUB_IDP_PUBLIC_PRIMARY_CERT))
                .add(ConfigOverride.config("europeanIdentity.signingKeyPairConfiguration.privateKeyConfiguration.type", "encoded"))
                .add(ConfigOverride.config("europeanIdentity.signingKeyPairConfiguration.privateKeyConfiguration.key", STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY))
                .add(ConfigOverride.config("europeanIdentity.signingKeyPairConfiguration.publicKeyConfiguration.type", "x509"))
                .add(ConfigOverride.config("europeanIdentity.signingKeyPairConfiguration.publicKeyConfiguration.cert", STUB_IDP_PUBLIC_PRIMARY_CERT))
                .add(ConfigOverride.config("database.url", "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"))
                .add(ConfigOverride.config("singleIdpJourney.enabled", "false"))
                .add(ConfigOverride.config("singleIdpJourney.serviceListUri", "http://localhost:"+fakeFrontend.getPort()+"/get-available-services"))
                .add(configOverrides)
                .build();
        return overrides.toArray(new ConfigOverride[overrides.size()]);
    }

    @Override
    public void before() throws Exception {
        trustStore.create();

        IdpStubsConfiguration idpStubsConfiguration = new TestIdpStubsConfiguration(stubIdps);
        try {
            FileUtils.write(STUB_IDPS_FILE, new ObjectMapper().writeValueAsString(idpStubsConfiguration), UTF_8);
            STUB_IDPS_FILE.deleteOnExit();

            InitializationService.initialize();

            metadataServer.reset();
            metadataServer.register(METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.before();
    }

    @Override
    public void after() {
        trustStore.delete();
        STUB_IDPS_FILE.delete();

        super.after();
    }

    public StubIdpAppExtension withStubIdp(StubIdp stubIdp) {
        this.stubIdps.add(stubIdp);
        return this;
    }

    public URI getMetadataPath() {
        return URI.create("http://localhost:" + metadataServer.getPort() + METADATA_PATH);
    }

    private static class TestIdpStubsConfiguration extends IdpStubsConfiguration {
        public TestIdpStubsConfiguration(List<StubIdp> idps) {
            this.stubIdps = idps;
        }
    }
}
