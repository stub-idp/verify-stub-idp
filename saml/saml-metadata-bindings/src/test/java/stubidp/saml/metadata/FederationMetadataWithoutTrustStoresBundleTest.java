package stubidp.saml.metadata;

import certificates.values.CACertificates;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.test.devpki.TestEntityIds;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.keystore.KeyStoreRule;
import stubidp.test.utils.keystore.builders.KeyStoreRuleBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.constants.Constants.APPLICATION_SAMLMETADATA_XML;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FederationMetadataWithoutTrustStoresBundleTest extends OpenSAMLRunner {

    private static final String VERIFY_METADATA_PATH = "/saml/metadata/sp";
    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();
    @RegisterExtension
    static final KeyStoreRule metadataKeyStoreRule;

    static {
        try {
            metadataKeyStoreRule = new KeyStoreRuleBuilder().withCertificate("metadata", CACertificates.TEST_METADATA_CA).withCertificate("root", CACertificates.TEST_ROOT_CA).build();
            metadataKeyStoreRule.beforeEach(null);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        verifyMetadataServer.reset();
        verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, APPLICATION_SAMLMETADATA_XML, new MetadataFactory().defaultMetadata());
    }

    private static final DropwizardAppExtension<TestConfiguration> APPLICATION_DROPWIZARD_APP_RULE = new DropwizardAppExtension<>(
        TestApplication.class,
        ResourceHelpers.resourceFilePath("test-app.yml"),
        ConfigOverride.config("metadata.uri", "http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH),
        ConfigOverride.config("metadata.trustStore.path", metadataKeyStoreRule::getAbsolutePath),
        ConfigOverride.config("metadata.trustStore.password", metadataKeyStoreRule::getPassword),
        ConfigOverride.config("metadata.unknownProperty", () -> "unknownValue")
    );

    private static Client client;

    @BeforeAll
    static void setUp() {
        client = new JerseyClientBuilder(APPLICATION_DROPWIZARD_APP_RULE.getEnvironment()).build(FederationMetadataWithoutTrustStoresBundleTest.class.getName() + "2");
    }

    @Test
    void shouldReadMetadataFromMetadataServerUsingTrustStoreBackedMetadataConfiguration() {
        Response response = client.target("http://localhost:" + APPLICATION_DROPWIZARD_APP_RULE.getLocalPort() +"/foo").request().get();
        assertThat(response.readEntity(String.class)).isEqualTo(TestEntityIds.HUB_ENTITY_ID);
    }

    public static class TestConfiguration extends Configuration {
        @JsonProperty("metadata")
        private TrustStoreBackedMetadataConfiguration metadataConfiguration;

        Optional<MetadataResolverConfiguration> getMetadataConfiguration() {
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
            private final MetadataResolver metadataResolver;
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
