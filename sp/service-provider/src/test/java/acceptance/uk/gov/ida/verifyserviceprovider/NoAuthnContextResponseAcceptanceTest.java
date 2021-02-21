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
import uk.gov.ida.verifyserviceprovider.dto.MatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedMatchingResponseBody;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Map;

import static acceptance.uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.NO_AUTHENTICATION_CONTEXT_ID;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

@Disabled
@ExtendWith(DropwizardExtensionsSupport.class)
public class NoAuthnContextResponseAcceptanceTest {

    public static MockMsaServer msaServer = new MockMsaServer();

    public static VerifyServiceProviderAppExtension application = new VerifyServiceProviderAppExtension(msaServer);

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
        complianceTool.initialiseWithDefaults();
    }

    @Test
    public void shouldRespondWithSuccessWhenNoAuthnContext() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = Map.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), NO_AUTHENTICATION_CONTEXT_ID),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", LEVEL_2.name()
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(TranslatedMatchingResponseBody.class).getScenario()).isEqualTo(MatchingScenario.CANCELLATION);
    }
}
