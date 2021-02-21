package acceptance.uk.gov.ida.verifyserviceprovider;

import acceptance.uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppExtension;
import acceptance.uk.gov.ida.verifyserviceprovider.services.ComplianceToolService;
import acceptance.uk.gov.ida.verifyserviceprovider.services.GenerateRequestService;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.jersey.errors.ErrorMessage;
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

import static acceptance.uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.MatchingScenario.SUCCESS_MATCH;

@Disabled
@ExtendWith(DropwizardExtensionsSupport.class)
public class TranslateResponseMultiTenantedSetupAcceptanceTest {
    private static final String configuredEntityIdOne = "http://service-entity-id-one";
    private static final String configuredEntityIdTwo = "http://service-entity-id-two";

    public static MockMsaServer msaServer = new MockMsaServer();

    public static VerifyServiceProviderAppExtension application = new VerifyServiceProviderAppExtension(msaServer, String.format("%s,%s", configuredEntityIdOne, configuredEntityIdTwo));

    private static Client client;
    private static ComplianceToolService complianceTool;
    private static GenerateRequestService generateRequestService;

    @BeforeAll
    public static void setUpBeforeClass() {
        client = application.client();
        complianceTool = new ComplianceToolService(client);
        generateRequestService = new GenerateRequestService(client);
    }

    @Test
    public void shouldHandleASuccessMatchResponseForCorrectProvidedEntityId() {
        String providedEntityId = configuredEntityIdTwo;
        complianceTool.initialiseWithEntityIdAndPid(providedEntityId, "some-expected-pid");

        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort(), providedEntityId);
        Map<String, String> translateResponseRequestData = Map.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", LEVEL_2.name(),
            "entityId", providedEntityId
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(TranslatedMatchingResponseBody.class)).isEqualTo(new TranslatedMatchingResponseBody(
            SUCCESS_MATCH,
            "some-expected-pid",
            LEVEL_2,
            null)
        );
    }

    @Test
    public void shouldReturn400IfNoEntityIdProvided() {
        complianceTool.initialiseWithEntityIdAndPid(configuredEntityIdOne, "some-expected-pid");
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort(), configuredEntityIdOne);
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

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(response.readEntity(ErrorMessage.class).getMessage()).isEqualTo("No entityId was provided, and there are several in config");
    }

    @Test
    public void shouldReturn400IfIncorrectEntityIdProvided() {
        complianceTool.initialiseWithEntityIdAndPid(configuredEntityIdTwo, "some-expected-pid");
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort(), configuredEntityIdTwo);
        Map<String, String> translateResponseRequestData = Map.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", LEVEL_2.name(),
            "entityId", "http://incorrect-entity-id"
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(response.readEntity(ErrorMessage.class).getMessage()).isEqualTo("Provided entityId: http://incorrect-entity-id is not listed in config");
    }
}
