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
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static acceptance.uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.BASIC_SUCCESSFUL_MATCH_WITH_LOA1_ID;
import static acceptance.uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.MatchingScenario.SUCCESS_MATCH;

@Disabled
@ExtendWith(DropwizardExtensionsSupport.class)
public class SuccessMatchAcceptanceTest {
    private static String configuredEntityId = "http://verify-service-provider";

    public static MockMsaServer msaServer = new MockMsaServer();

    public static VerifyServiceProviderAppExtension application = new VerifyServiceProviderAppExtension(msaServer, configuredEntityId);

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
        complianceTool.initialiseWithPid("some-expected-pid");
    }

    @Test
    public void shouldHandleASuccessMatchResponseForDefaultEntityId() {
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
            "some-expected-pid",
            LEVEL_2,
            null)
        );
    }

    @Test
    public void shouldHandleASuccessMatchResponseForCorrectProvidedEntityId() {
        String providedEntityId = configuredEntityId;
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
    public void shouldHandleLoA1SuccessMatchResponse() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = Map.of(
                "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA1_ID),
                "requestId", requestResponseBody.getRequestId(),
                "levelOfAssurance", LEVEL_1.name()
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
                LEVEL_1,
                null)
        );
    }

    @Test
    public void shouldReturnAnErrorWhenTranslatingLowerLoAThanRequested() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = Map.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA1_ID),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", LEVEL_2.name()
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(errorMessage.getMessage()).isEqualTo("Expected Level of Assurance to be at least LEVEL_2, but was LEVEL_1");
    }

    @Test
    public void shouldReturnAnErrorWhenInvalidEntityIdProvided() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = Map.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", LEVEL_2.name(),
            "entityId", "invalidEntityId"
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(errorMessage.getMessage()).isEqualTo("Provided entityId: invalidEntityId is not listed in config");
    }

    @Test
    public void shouldReturnAnErrorWhenNullRequestIdIsProvided() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());

        Map<String, String> translateResponseRequestData = new HashMap<>();
        translateResponseRequestData.put(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID));
        translateResponseRequestData.put("requestId", null);
        translateResponseRequestData.put("levelOfAssurance", LEVEL_2.name());

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(422);
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(422);
        assertThat(errorMessage.getMessage()).isEqualTo("requestId may not be null");
    }

    @Test
    public void shouldReturnAnErrorForTheNullRequestIdProvidedRegardlessOfIncorrectSaml() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());

        Map<String, String> translateResponseRequestData = new HashMap<>();
        translateResponseRequestData.put("samlResponse", "not a SAML response");
        translateResponseRequestData.put("requestId", null);
        translateResponseRequestData.put("levelOfAssurance", LEVEL_2.name());

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(422);
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(422);
        assertThat(errorMessage.getMessage()).isEqualTo("requestId may not be null");
    }

    @Test
    public void shouldReturnAnErrorForAlteredSamlResponses() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());

        String saml = complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID);

        StringToOpenSamlObjectTransformer<org.opensaml.saml.saml2.core.Response> deserializer = ResponseFactory.createStringToResponseTransformer();
        org.opensaml.saml.saml2.core.Response authenticationResponse = deserializer.apply(saml);
        authenticationResponse.setInResponseTo(null);

        XmlObjectToBase64EncodedStringTransformer<org.opensaml.saml.saml2.core.Response> reserializer = new XmlObjectToBase64EncodedStringTransformer<>();
        String alteredEncodedSaml = reserializer.apply(authenticationResponse);

        Map<String, String> translateResponseRequestData = new HashMap<>();
        translateResponseRequestData.put("samlResponse", alteredEncodedSaml);
        translateResponseRequestData.put("requestId", requestResponseBody.getRequestId());
        translateResponseRequestData.put("levelOfAssurance", LEVEL_2.name());

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(errorMessage.getMessage()).isEqualTo("SAML Validation Specification: Message signature is not signed\n" +
            "DocumentReference{documentName='Hub Service Profile 1.1a', documentSection=''}");
    }

    @Test
    public void shouldReturnAnErrorForNonSamlResponses() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());

        Map<String, String> translateResponseRequestData = new HashMap<>();
        translateResponseRequestData.put("samlResponse", "not a SAML response");
        translateResponseRequestData.put("requestId", requestResponseBody.getRequestId());
        translateResponseRequestData.put("levelOfAssurance", LEVEL_2.name());

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(errorMessage.getMessage()).startsWith("SAML Validation Specification: Unable to deserialize string into OpenSaml object");
    }

    @Test
    public void shouldReturnAnErrorWhenAnEmptyRequestIdIsProvided() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());

        Map<String, String> translateResponseRequestData = new HashMap<>();
        translateResponseRequestData.put(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID));
        translateResponseRequestData.put("requestId", "");
        translateResponseRequestData.put("levelOfAssurance", LEVEL_2.name());

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(errorMessage.getMessage()).isEqualTo(
            String.format("Expected InResponseTo to be %s, but was %s", "", requestResponseBody.getRequestId()));
    }

    @Test
    public void shouldReturnAnErrorWhenAnIncorrectRequestIdIsProvided() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());

        String falseRequestId = "_" + UUID.randomUUID().toString();

        Map<String, String> translateResponseRequestData = new HashMap<>();
        translateResponseRequestData.put(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID));
        translateResponseRequestData.put("requestId", falseRequestId);
        translateResponseRequestData.put("levelOfAssurance", LEVEL_2.name());

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(errorMessage.getMessage()).isEqualTo(
            String.format("Expected InResponseTo to be %s, but was %s", falseRequestId, requestResponseBody.getRequestId()));
    }
}
