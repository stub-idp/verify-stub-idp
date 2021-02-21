package acceptance.uk.gov.ida.verifyserviceprovider;

import acceptance.uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppExtension;
import acceptance.uk.gov.ida.verifyserviceprovider.services.ComplianceToolService;
import acceptance.uk.gov.ida.verifyserviceprovider.services.GenerateRequestService;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedMatchingResponseBody;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Map;

import static acceptance.uk.gov.ida.verifyserviceprovider.builders.ComplianceToolV1InitialisationRequestBuilder.aComplianceToolV1InitialisationRequest;
import static acceptance.uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.MatchingScenario.SUCCESS_MATCH;

@Disabled
@ExtendWith(DropwizardExtensionsSupport.class)
public class SecondaryEncryptionKeyAcceptanceTest {

    public static MockMsaServer msaServer = new MockMsaServer();

    public static VerifyServiceProviderAppExtension application = new VerifyServiceProviderAppExtension(msaServer, TEST_RP_MS_PRIVATE_ENCRYPTION_KEY, "http://verify-service-provider");

    private static Client client;
    private static ComplianceToolService complianceTool;
    private static GenerateRequestService generateRequestService;

    @BeforeAll
    public static void setUpBeforeClass() {
        client = application.client();
        complianceTool = new ComplianceToolService(client);
        generateRequestService = new GenerateRequestService(client);
    }

    @BeforeEach
    public void setUp() {
        complianceTool.initialiseWith(
                aComplianceToolV1InitialisationRequest()
                .withEncryptionCertificate(TEST_RP_MS_PUBLIC_ENCRYPTION_CERT)
                .build()
        );
    }

    @Test
    public void shouldHandleASuccessMatchResponseSignedWithSecondaryKey() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = Map.of(
                "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID),
                "requestId", requestResponseBody.getRequestId(),
                "levelOfAssurance", LEVEL_2.name()
        );

        Response response = client
                .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
                .request()
                .buildPost(json(translateResponseRequestData))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(TranslatedMatchingResponseBody.class)).isEqualTo(new TranslatedMatchingResponseBody(
                SUCCESS_MATCH,
                "default-expected-pid",
                LEVEL_2,
                null)
        );
    }
}
