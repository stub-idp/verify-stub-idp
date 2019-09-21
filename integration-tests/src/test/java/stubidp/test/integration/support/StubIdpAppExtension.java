package stubidp.test.integration.support;

import certificates.values.CACertificates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;

public class StubIdpAppExtension extends DropwizardAppExtension<StubIdpConfiguration> {

    private static final String VERIFY_METADATA_PATH = "/saml/metadata/sp";
    private static final String EIDAS_METADATA_PATH = "/saml/metadata/eidas";

    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();
    private static final HttpStubRule eidasMetadataServer = new HttpStubRule();
    private static final KeyStoreResource trustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final File STUB_IDPS_FILE = new File(System.getProperty("java.io.tmpdir"), "stub-idps.yml");

    private final List<StubIdp> stubIdps = new ArrayList<>();

    private static final HttpStubRule fakeFrontend = new HttpStubRule();

    public StubIdpAppExtension() {
        this(Map.of());
    }

    public StubIdpAppExtension(Map<String, String> configOverrides) {
        super(StubIdpApplication.class, "../configuration/stub-idp.yml", withDefaultOverrides(configOverrides));
        try {
            fakeFrontend.register("/get-available-services", 200, "application/json", "[]");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static ConfigOverride[] withDefaultOverrides(Map<String, String> configOverrides) {
        Map<String, String> config = Map.<String, String>ofEntries(
                Map.entry("metadata.uri", "http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH),
                Map.entry("hubEntityId", HUB_ENTITY_ID),
                Map.entry("basicAuthEnabledForUserResource", "true"),
                Map.entry("server.requestLog.appenders[0].type", "console"),
                Map.entry("server.applicationConnectors[0].port", "0"),
                Map.entry("server.adminConnectors[0].port", "0"),
                Map.entry("logging.appenders[0].type", "console"),
                Map.entry("stubIdpsYmlFileLocation", STUB_IDPS_FILE.getAbsolutePath()),
                Map.entry("metadata.trustStore.store", trustStore.getAbsolutePath()),
                Map.entry("metadata.trustStore.password", trustStore.getPassword()),
                Map.entry("europeanIdentity.enabled", "true"),
                Map.entry("europeanIdentity.hubConnectorEntityId", HUB_ENTITY_ID),
                Map.entry("europeanIdentity.stubCountryBaseUrl", "http://localhost:0"),
                Map.entry("europeanIdentity.metadata.uri", "http://localhost:" + eidasMetadataServer.getPort() + EIDAS_METADATA_PATH),
                Map.entry("europeanIdentity.metadata.expectedEntityId", HUB_ENTITY_ID),
                Map.entry("europeanIdentity.metadata.trustStore.store", trustStore.getAbsolutePath()),
                Map.entry("europeanIdentity.metadata.trustStore.password", trustStore.getPassword()),
                Map.entry("signingKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                Map.entry("signingKeyPairConfiguration.privateKeyConfiguration.key", STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY),
                Map.entry("signingKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                Map.entry("signingKeyPairConfiguration.publicKeyConfiguration.cert", STUB_IDP_PUBLIC_PRIMARY_CERT),
                Map.entry("europeanIdentity.signingKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                Map.entry("europeanIdentity.signingKeyPairConfiguration.privateKeyConfiguration.key", STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY),
                Map.entry("europeanIdentity.signingKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                Map.entry("europeanIdentity.signingKeyPairConfiguration.publicKeyConfiguration.cert", STUB_IDP_PUBLIC_PRIMARY_CERT),
                Map.entry("database.url", "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"),
                Map.entry("singleIdpJourney.enabled", "false"),
                Map.entry("singleIdpJourney.serviceListUri", "http://localhost:"+fakeFrontend.getPort()+"/get-available-services"));
        config = new HashMap<>(config);
        config.putAll(configOverrides);
        final List<ConfigOverride> overrides = config.entrySet().stream()
                .map(o -> ConfigOverride.config(o.getKey(), o.getValue()))
                .collect(Collectors.toUnmodifiableList());
        return overrides.toArray(new ConfigOverride[config.size()]);
    }

    @Override
    public void before() throws Exception {
        trustStore.create();

        IdpStubsConfiguration idpStubsConfiguration = new TestIdpStubsConfiguration(stubIdps);
        try {
            FileUtils.write(STUB_IDPS_FILE, new ObjectMapper().writeValueAsString(idpStubsConfiguration), UTF_8);
            STUB_IDPS_FILE.deleteOnExit();

            InitializationService.initialize();

            verifyMetadataServer.reset();
            eidasMetadataServer.reset();
            verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());
            eidasMetadataServer.register(EIDAS_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());
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

    public URI getVerifyMetadataPath() {
        return URI.create("http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH);
    }

    public URI getEidasMetadataPath() {
        return URI.create("http://localhost:" + eidasMetadataServer.getPort() + EIDAS_METADATA_PATH);
    }

    private static class TestIdpStubsConfiguration extends IdpStubsConfiguration {
        public TestIdpStubsConfiguration(List<StubIdp> idps) {
            this.stubIdps = idps;
        }
    }
}
