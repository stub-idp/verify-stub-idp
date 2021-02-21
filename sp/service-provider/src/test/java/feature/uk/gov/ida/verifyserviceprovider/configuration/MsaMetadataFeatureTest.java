package feature.uk.gov.ida.verifyserviceprovider.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import common.uk.gov.ida.verifyserviceprovider.servers.MockVerifyHubServer;
import common.uk.gov.ida.verifyserviceprovider.utils.EnvironmentHelper;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.test.utils.keystore.KeyStoreResource;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;

import static certificates.values.CACertificates.TEST_CORE_CA;
import static certificates.values.CACertificates.TEST_IDP_CA;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ConfigOverride.config;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.CertificateBuilder.aCertificate;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;

@Disabled("cannot setEnv on java16+")
public class MsaMetadataFeatureTest extends OpenSAMLRunner {

    private final String HEALTHCHECK_URL = "http://localhost:%d/admin/healthcheck";

    private static final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    private static final MockVerifyHubServer hubServer = new MockVerifyHubServer();

    private DropwizardTestSupport<VerifyServiceProviderConfiguration> applicationTestSupport;
    private final EnvironmentHelper environmentHelper = new EnvironmentHelper();

    @BeforeEach
    public void setUp() {
        wireMockServer.start();
        hubServer.start();
        hubServer.serveDefaultMetadata();

        KeyStoreResource metadataTrustStore = aKeyStoreResource()
                .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
                .build();
        KeyStoreResource hubTrustStore = aKeyStoreResource()
                .withCertificate("VERIFY-HUB", aCertificate().withCertificate(TEST_CORE_CA).build().getCertificate())
                .build();
        KeyStoreResource idpTrustStore = aKeyStoreResource()
                .withCertificate("VERIFY-IDP", aCertificate().withCertificate(TEST_IDP_CA).build().getCertificate())
                .build();

        metadataTrustStore.create();
        hubTrustStore.create();
        idpTrustStore.create();

        this.applicationTestSupport = new DropwizardTestSupport<>(
            VerifyServiceProviderApplication.class,
                ResourceHelpers.resourceFilePath("verify-service-provider-with-msa.yml"),
            config("server.connector.port", "0"),
            config("verifyHubConfiguration.metadata.uri", format("http://localhost:%s/SAML2/metadata", hubServer.port())),
            config("msaMetadata.uri", getMsaMetadataUrl()),
            config("verifyHubConfiguration.metadata.expectedEntityId", HUB_ENTITY_ID),
            config("msaMetadata.expectedEntityId", MockMsaServer.MSA_ENTITY_ID),
            config("verifyHubConfiguration.metadata.trustStore.path", metadataTrustStore.getAbsolutePath()),
            config("verifyHubConfiguration.metadata.trustStore.password", metadataTrustStore.getPassword()),
            config("verifyHubConfiguration.metadata.hubTrustStore.path", hubTrustStore.getAbsolutePath()),
            config("verifyHubConfiguration.metadata.hubTrustStore.password", hubTrustStore.getPassword()),
            config("verifyHubConfiguration.metadata.idpTrustStore.path", idpTrustStore.getAbsolutePath()),
            config("verifyHubConfiguration.metadata.idpTrustStore.password", idpTrustStore.getPassword())
        );

        environmentHelper.setEnv(new HashMap<>() {{
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("MSA_METADATA_URL", "some-msa-metadata-url");
            put("MSA_ENTITY_ID", "some-msa-entity-id");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]");
            put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
        }});
    }

    private String getMsaMetadataUrl() {
        return format("http://localhost:%s/matching-service/metadata", wireMockServer.port());
    }

    @AfterEach
    public void tearDown() {
        applicationTestSupport.after();
        wireMockServer.stop();
    }

    @Test
    public void shouldFailHealthcheckWhenMsaMetadataUnavailable() throws Exception {
        wireMockServer.stubFor(
            get(urlEqualTo("/matching-service/metadata"))
                .willReturn(aResponse()
                    .withStatus(500)
                )
        );

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = format("\"%s\":{\"healthy\":false", getMsaMetadataUrl());

        wireMockServer.verify(getRequestedFor(urlEqualTo("/matching-service/metadata")));

        assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }

    @Test
    public void shouldPassHealthcheckWhenMsaMetadataAvailable() throws Exception {
        wireMockServer.stubFor(
            get(urlEqualTo("/matching-service/metadata"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(MockMsaServer.msaMetadata())
                )
        );

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = format("\"%s\":{\"healthy\":true", getMsaMetadataUrl());

        wireMockServer.verify(getRequestedFor(urlEqualTo("/matching-service/metadata")));

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }
}
