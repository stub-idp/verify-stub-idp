package stubidp.saml.metadata;

import certificates.values.CACertificates;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.saml.metadata.support.WireMockExtension;
import stubidp.saml.metadata.test.factories.metadata.MetadataFactory;
import stubidp.saml.utils.OpenSAMLRunner;
import stubidp.test.devpki.TestEntityIds;
import stubidp.test.utils.keystore.KeyStoreRule;
import stubidp.test.utils.keystore.builders.KeyStoreRuleBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FederationMetadataBundleTest extends OpenSAMLRunner {

    @RegisterExtension
    public static final WireMockExtension metadataResource;
    @RegisterExtension
    public static KeyStoreRule metadataKeyStoreRule;
    @RegisterExtension
    public static KeyStoreRule hubKeyStoreRule;
    @RegisterExtension
    public static KeyStoreRule idpKeyStoreRule;

    static {
        try {
            metadataKeyStoreRule = new KeyStoreRuleBuilder().withCertificate("metadata", CACertificates.TEST_METADATA_CA).withCertificate("root", CACertificates.TEST_ROOT_CA).build();
            metadataKeyStoreRule.beforeEach(null);
            hubKeyStoreRule = new KeyStoreRuleBuilder().withCertificate("hub", CACertificates.TEST_CORE_CA).withCertificate("root", CACertificates.TEST_ROOT_CA).build();
            hubKeyStoreRule.beforeEach(null);
            idpKeyStoreRule = new KeyStoreRuleBuilder().withCertificate("idp", CACertificates.TEST_IDP_CA).withCertificate("root", CACertificates.TEST_ROOT_CA).build();
            idpKeyStoreRule.beforeEach(null);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        metadataResource = new WireMockExtension(WireMockConfiguration.options().dynamicPort());
        // starting here to get the port to pass to the app
        metadataResource.start();
        metadataResource.stubFor(get(urlEqualTo("/metadata")).willReturn(aResponse().withBody(new MetadataFactory().defaultMetadata())));
    }

    public static final DropwizardAppExtension<TestConfiguration> APPLICATION_DROPWIZARD_APP_RULE = new DropwizardAppExtension<>(
        TestApplication.class,
        ResourceHelpers.resourceFilePath("test-app.yml"),
        ConfigOverride.config("metadata.uri", () -> "http://localhost:" + metadataResource.port() + "/metadata"),
        ConfigOverride.config("metadata.trustStore.path", () -> metadataKeyStoreRule.getAbsolutePath()),
        ConfigOverride.config("metadata.trustStore.password", () -> metadataKeyStoreRule.getPassword()),
        ConfigOverride.config("metadata.unknownProperty", () -> "unknownValue"),
        ConfigOverride.config("metadata.hubTrustStore.path", () -> hubKeyStoreRule.getAbsolutePath()),
        ConfigOverride.config("metadata.hubTrustStore.password", () -> hubKeyStoreRule.getPassword()),
        ConfigOverride.config("metadata.idpTrustStore.path", () -> idpKeyStoreRule.getAbsolutePath()),
        ConfigOverride.config("metadata.idpTrustStore.password", () -> idpKeyStoreRule.getPassword())
    );

    private static Client client;

    @BeforeAll
    public static void setUp() {
        client = new JerseyClientBuilder(APPLICATION_DROPWIZARD_APP_RULE.getEnvironment())
                .withProperty(ClientProperties.CONNECT_TIMEOUT, 10*1000) // for my slow chromebook
                .withProperty(ClientProperties.READ_TIMEOUT, 10*1000) // for my slow chromebook
                .build(FederationMetadataBundleTest.class.getName() + "2");
    }

    @Test
    public void shouldReadMetadataFromMetadataServerUsingTrustStoreBackedMetadataConfiguration() {
        Response response = client.target("http://localhost:" + APPLICATION_DROPWIZARD_APP_RULE.getLocalPort() +"/foo").request().get();
        assertThat(response.readEntity(String.class)).isEqualTo(TestEntityIds.HUB_ENTITY_ID);
    }

    public static class TestConfiguration extends Configuration {
        @JsonProperty("metadata")
        private MultiTrustStoresBackedMetadataConfiguration metadataConfiguration;

        public Optional<MetadataResolverConfiguration> getMetadataConfiguration() {
            return Optional.ofNullable(metadataConfiguration);
        }
    }

    public static class TestApplication extends Application<TestConfiguration> {
        private MetadataResolverBundle<TestConfiguration> bundle;

        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            super.initialize(bootstrap);
            bundle = new MetadataResolverBundle<>(TestConfiguration::getMetadataConfiguration);
            bootstrap.addBundle(bundle);
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) {
            environment.jersey().register(new TestResource(bundle.getMetadataResolver()));
        }

        @Path("/")
        public static class TestResource {
            private MetadataResolver metadataResolver;
            TestResource(MetadataResolver metadataResolver) {
                this.metadataResolver = metadataResolver;
            }

            @Path("/foo")
            @GET
            public String getMetadata() throws ResolverException {
                return metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(TestEntityIds.HUB_ENTITY_ID))).getEntityID();
            }
        }
    }
}
