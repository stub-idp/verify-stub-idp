package unit.uk.gov.ida.verifyserviceprovider.resources;

import com.google.common.collect.ImmutableSet;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.dto.MatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JsonProcessingExceptionMapper;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

@ExtendWith({MockitoExtension.class, DropwizardExtensionsSupport.class})
public class TranslateSamlResponseResourceTest extends OpenSAMLRunner {

    private final ResponseService responseService = mock(ResponseService.class);

    private final EntityIdService entityIdService = mock(EntityIdService.class);

    private static final String defaultEntityId = "http://default-entity-id";

    private final ResourceExtension resourceExtension = ResourceExtension.builder()
            .addProvider(JerseyViolationExceptionMapper.class)
                .addProvider(JsonProcessingExceptionMapper.class)
                .addProvider(InvalidEntityIdExceptionMapper.class)
                .addResource(new TranslateSamlResponseResource(responseService, entityIdService))
            .build();

    @BeforeEach
    public void mockEntityIdService() {
        when(entityIdService.getEntityId(any(TranslateSamlResponseBody.class))).thenReturn(defaultEntityId);
    }

    @AfterEach
    public void setup() {
        reset(responseService);
    }

    @Test
    public void shouldUseResponseServiceToTranslateSaml() {
        Map<String, String> translateResponseRequest = Map.of("samlResponse", "some-saml-response",
            "requestId", "some-request-id",
            "levelOfAssurance", LEVEL_2.name());

        when(responseService.convertTranslatedResponseBody(any(), eq("some-request-id"), eq(LEVEL_2), eq(defaultEntityId)))
            .thenReturn(new TranslatedMatchingResponseBody(MatchingScenario.SUCCESS_MATCH, "some-request-id", LEVEL_2, null));

        Response response = resourceExtension.client()
            .target("/translate-response")
            .request()
            .post(json(translateResponseRequest));

        verify(responseService, times(1)).convertTranslatedResponseBody(
            translateResponseRequest.get("samlResponse"), "some-request-id", LEVEL_2, defaultEntityId
        );
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldReturn400WhenSamlValidationExceptionThrown() {
        Map<String, String> translateResponseRequest = Map.of("samlResponse", "some-saml-response",
                "requestId", "some-request-id",
                "levelOfAssurance", LEVEL_2.name());

        when(responseService.convertTranslatedResponseBody(any(), eq("some-request-id"), eq(LEVEL_2), eq(defaultEntityId)))
                .thenThrow(new SamlResponseValidationException("Some error."));

        Response response = resourceExtension.client()
                .target("/translate-response")
                .request()
                .post(json(translateResponseRequest));

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        ErrorMessage actualError = response.readEntity(ErrorMessage.class);
        assertThat(actualError.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(actualError.getMessage()).isEqualTo("Some error.");
    }

    @Test
    public void shouldReturn400WhenSamlTransformationErrorExceptionThrown() {
        Map<String, String> translateResponseRequest = Map.of("samlResponse", "some-saml-response",
                "requestId", "some-request-id",
                "levelOfAssurance", LEVEL_2.name());

        when(responseService.convertTranslatedResponseBody(any(), eq("some-request-id"), eq(LEVEL_2), eq(defaultEntityId)))
                .thenThrow(new SamlTransformationErrorException("Some error.", Level.ERROR));

        Response response = resourceExtension.client()
            .target("/translate-response")
            .request()
            .post(json(translateResponseRequest));

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        ErrorMessage actualError = response.readEntity(ErrorMessage.class);
        assertThat(actualError.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(actualError.getMessage()).isEqualTo("Some error.");
    }

    @Test
    public void shouldReturn400WhenCalledWithEmptyJson() {
        Response response = resourceExtension.client()
            .target("/translate-response")
            .request()
            .post(json("{}"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);

        ErrorMessage actualErrorMessage = response.readEntity(ErrorMessage.class);
        assertThat(actualErrorMessage.getCode()).isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);

        Set<String> expectedErrors = ImmutableSet.of("requestId must not be null", "samlResponse must not be null", "levelOfAssurance must not be null");
        Set<String> actualErrors = Arrays.stream(actualErrorMessage.getMessage().split(", ")).collect(Collectors.toSet());
        assertThat(actualErrors).isEqualTo(expectedErrors);
    }

}
