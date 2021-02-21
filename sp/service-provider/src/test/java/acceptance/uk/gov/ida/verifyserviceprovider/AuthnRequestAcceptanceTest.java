package acceptance.uk.gov.ida.verifyserviceprovider;

import acceptance.uk.gov.ida.verifyserviceprovider.dto.ComplianceToolResponseDto;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.test.utils.keystore.KeyStoreResource;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static acceptance.uk.gov.ida.verifyserviceprovider.builders.ComplianceToolV1InitialisationRequestBuilder.aComplianceToolV1InitialisationRequest;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.CertificateBuilder.aCertificate;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;

@Disabled
@ExtendWith(DropwizardExtensionsSupport.class)
public class AuthnRequestAcceptanceTest {

    private static String COMPLIANCE_TOOL_HOST = "https://compliance-tool-integration.cloudapps.digital";
    private static String SINGLE_ENTITY_ID = "http://default-entity-id";
    private static String HASHING_ENTITY_ID = "http://default-entity-id";
    private static String MULTI_ENTITY_ID_1 = "http://service-entity-id-one";
    private static String MULTI_ENTITY_ID_2 = "http://service-entity-id-two";

    private static final KeyStoreResource KEY_STORE_RESOURCE = aKeyStoreResource()
        .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
        .build();
    static {
        KEY_STORE_RESOURCE.create();
    }

    public final DropwizardAppExtension<VerifyServiceProviderConfiguration> singleTenantApplication = new DropwizardAppExtension<>(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            ConfigOverride.config("server.connector.port", String.valueOf(0)),
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
            ConfigOverride.config("serviceEntityIds", SINGLE_ENTITY_ID),
            ConfigOverride.config("hashingEntityId", HASHING_ENTITY_ID),
            ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("europeanIdentity.enabled", "false"),
            ConfigOverride.config("europeanIdentity.hubConnectorEntityId", "dummyEntity"),
            ConfigOverride.config("europeanIdentity.trustAnchorUri", "http://dummy.com"),
            ConfigOverride.config("europeanIdentity.metadataSourceUri", "http://dummy.com"),
            ConfigOverride.config("europeanIdentity.trustStore.path", KEY_STORE_RESOURCE.getAbsolutePath()),
            ConfigOverride.config("europeanIdentity.trustStore.password", KEY_STORE_RESOURCE.getPassword())
    );

    public final DropwizardAppExtension<VerifyServiceProviderConfiguration> multiTenantApplication = new DropwizardAppExtension<>(
        VerifyServiceProviderApplication.class,
        "verify-service-provider.yml",
        ConfigOverride.config("server.connector.port", String.valueOf(0)),
        ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
        ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
        ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
        ConfigOverride.config("serviceEntityIds", String.format("%s,%s", MULTI_ENTITY_ID_1, MULTI_ENTITY_ID_2)),
        ConfigOverride.config("hashingEntityId", HASHING_ENTITY_ID),
        ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
        ConfigOverride.config("europeanIdentity.enabled", "false"),
        ConfigOverride.config("europeanIdentity.hubConnectorEntityId", "dummyEntity"),
        ConfigOverride.config("europeanIdentity.trustAnchorUri", "http://dummy.com"),
        ConfigOverride.config("europeanIdentity.metadataSourceUri", "http://dummy.com"),
        ConfigOverride.config("europeanIdentity.trustStore.path", KEY_STORE_RESOURCE.getAbsolutePath()),
        ConfigOverride.config("europeanIdentity.trustStore.password", KEY_STORE_RESOURCE.getPassword())
    );

    @Test
    public void shouldGenerateValidAuthnRequestWhenNoParams() throws Exception {
        Client client = new JerseyClientBuilder(singleTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithDefaultEntityId(client);

        Response generateRequestResponse = client
                .target(URI.create(String.format("http://localhost:%d/generate-request", singleTenantApplication.getLocalPort())))
                .request()
                .buildPost(Entity.json(null))
                .invoke();

        RequestResponseBody authnSaml = generateRequestResponse.readEntity(RequestResponseBody.class);
        assertThat(authnSaml.getRequestId()).isNotBlank();

        Response complianceToolResponse = client
                .target(authnSaml.getSsoLocation())
                .request()
                .buildPost(Entity.form(new MultivaluedHashMap<>(Map.of("SAMLRequest", authnSaml.getSamlRequest()))))
                .invoke();

        final ComplianceToolResponseDto complianceToolResponseDto = complianceToolResponse.readEntity(ComplianceToolResponseDto.class);
        assertThat(complianceToolResponseDto.getStatus().getMessage()).isNull();
        assertThat(complianceToolResponseDto.getStatus().getStatus()).isEqualTo("PASSED");
    }


    @Test
    public void shouldGenerateValidAuthnRequestUsingDefaultEntityId() throws Exception {
        Client client = new JerseyClientBuilder(singleTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithDefaultEntityId(client);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", singleTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(null)))
            .invoke();

        RequestResponseBody authnSaml = authnResponse.readEntity(RequestResponseBody.class);
        assertThat(authnSaml.getRequestId()).isNotBlank();

        Response complianceToolResponse = client
            .target(authnSaml.getSsoLocation())
            .request()
            .buildPost(Entity.form(new MultivaluedHashMap<>(Map.of("SAMLRequest", authnSaml.getSamlRequest()))))
            .invoke();

        final ComplianceToolResponseDto complianceToolResponseDto = complianceToolResponse.readEntity(ComplianceToolResponseDto.class);
        assertThat(complianceToolResponseDto.getStatus().getMessage()).isNull();
        assertThat(complianceToolResponseDto.getStatus().getStatus()).isEqualTo("PASSED");
    }

    @Test
    public void shouldGenerateValidAuthnRequestWhenPassedAnEntityId() throws Exception {
        Client client = new JerseyClientBuilder(multiTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithEntityId(client, MULTI_ENTITY_ID_1);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", multiTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(MULTI_ENTITY_ID_1)))
            .invoke();

        RequestResponseBody authnSaml = authnResponse.readEntity(RequestResponseBody.class);
        assertThat(authnSaml.getRequestId()).isNotBlank();

        Response complianceToolResponse = client
            .target(authnSaml.getSsoLocation())
            .request()
            .buildPost(Entity.form(new MultivaluedHashMap<>(Map.of("SAMLRequest", authnSaml.getSamlRequest()))))
            .invoke();

        final ComplianceToolResponseDto complianceToolResponseDto = complianceToolResponse.readEntity(ComplianceToolResponseDto.class);
        assertThat(complianceToolResponseDto.getStatus().getMessage()).isNull();
        assertThat(complianceToolResponseDto.getStatus().getStatus()).isEqualTo("PASSED");
    }

    @Test
    public void shouldReturn400WhenPassedNoEntityIdForMultiTenantApplication() throws Exception {
        Client client = new JerseyClientBuilder(multiTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithEntityId(client, MULTI_ENTITY_ID_1);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", multiTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(null)))
            .invoke();

        assertThat(authnResponse.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldReturn400WhenPassedInvalidEntityIdForMultiTenantApplication() throws Exception {
        Client client = new JerseyClientBuilder(multiTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithEntityId(client, MULTI_ENTITY_ID_1);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", multiTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody("not a valid entityID")))
            .invoke();

        assertThat(authnResponse.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    private void setupComplianceToolWithDefaultEntityId(Client client) throws Exception {
        setupComplianceToolWithEntityId(client, SINGLE_ENTITY_ID);
    }

    private void setupComplianceToolWithEntityId(Client client, String entityId) throws Exception {
        Entity<Map<String, Object>> initializationRequest = aComplianceToolV1InitialisationRequest().withEntityId(entityId).build();

        Response complianceToolResponse = client
            .target(URI.create(String.format("%s/%s", COMPLIANCE_TOOL_HOST, "service-test-data")))
            .request()
            .buildPost(initializationRequest)
            .invoke();

        assertThat(complianceToolResponse.getStatus()).isEqualTo(OK.getStatusCode());
    }
}
