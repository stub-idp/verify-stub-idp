package stubidp.utils.rest.jerseyclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.utils.rest.common.ErrorStatusDto;
import stubidp.utils.rest.common.ExceptionType;
import stubidp.utils.rest.exceptions.ApplicationException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.utils.rest.exceptions.ApplicationException.createExceptionFromErrorStatusDto;

class JsonResponseProcessorTest {

    private final URI uri = URI.create("http://somedomain.com");
    private final UUID errorId = UUID.randomUUID();
    private final ExceptionType exceptionType = ExceptionType.INVALID_SAML;

    private JsonResponseProcessor responseProcessor;

    @BeforeEach
    void setUp() {
        responseProcessor = new JsonResponseProcessor(new ObjectMapper());
    }

    @Test
    void getJsonEntity_shouldThrowExceptionBasedOnErrorStatusDtoIfOneIsReturned() throws Exception {
        Response response = createMockResponse(400, ErrorStatusDto.createAuditedErrorStatus(errorId, exceptionType));

        final ApplicationException e = Assertions.assertThrows(ApplicationException.class, () -> responseProcessor.getJsonEntity(uri, null, Object.class, response));

        verify(response, times(1)).readEntity(String.class);
        assertThat(e.getExceptionType()).isEqualTo(exceptionType);
        assertThat(e.getErrorId()).isEqualTo(errorId);
        assertThat(e.isAudited()).isEqualTo(true);
    }

    @Test
    void getJson_shouldThrowUnauditedErrorExceptionIfClientErrorResponseEntityAsStringIsReturned() throws Exception {
        Response response = createMockResponse(400, "Some Entity");
        ApplicationException applicationException = createExceptionFromErrorStatusDto(
                ErrorStatusDto.createUnauditedErrorStatus(UUID.randomUUID(),
                        ExceptionType.CLIENT_ERROR,
                        response.readEntity(String.class)), uri);

        final ApplicationException e = Assertions.assertThrows(ApplicationException.class, () -> responseProcessor.getJsonEntity(uri, null, Object.class, response));

        verify(response, times(2)).readEntity(String.class);
        assertThat(e.getExceptionType()).isEqualTo(applicationException.getExceptionType());
        assertThat(e.getMessage()).isEqualTo(applicationException.getMessage());
        assertThat(e.getUri()).isEqualTo(applicationException.getUri());
        assertThat(e.isAudited()).isEqualTo(applicationException.isAudited());
        assertThat(e.requiresAuditing()).isEqualTo(applicationException.requiresAuditing());
    }

    @Test
    void getJson_shouldReturnApplicationExceptionWithErrorStatusDtoIfResponseContainsMalformedErrorStatusDtoJsonString() throws Exception {
        Response clientResponse = createMockResponse(400, "{\"extra\":\"shouldn't be here\",\"audited\":true,\"errorId\":\"1357ad59-5652-4bde-ac19-593c2316a389\",\"exceptionType\":\"INVALID_SAML\",\"clientMessage\":\"\"}");
        ApplicationException applicationException = createExceptionFromErrorStatusDto(
                ErrorStatusDto.createUnauditedErrorStatus(UUID.randomUUID(),
                        ExceptionType.CLIENT_ERROR,
                        clientResponse.readEntity(String.class)), uri);

        final ApplicationException e = Assertions.assertThrows(ApplicationException.class, () -> responseProcessor.getJsonEntity(uri, null, Object.class, clientResponse));

        verify(clientResponse, times(2)).readEntity(String.class);
        assertThat(e.getExceptionType()).isEqualTo(applicationException.getExceptionType());
        assertThat(e.getMessage()).isEqualTo(applicationException.getMessage());
        assertThat(e.getUri()).isEqualTo(applicationException.getUri());
        assertThat(e.isAudited()).isEqualTo(applicationException.isAudited());
        assertThat(e.requiresAuditing()).isEqualTo(applicationException.requiresAuditing());
    }

    @Test
    void getJson_shouldThrowUnauditedErrorExceptionIfServerErrorResponseAsStringIsReturned() throws Exception {
        Response clientResponse = createMockResponse(500, "There has been some internal server error");

        final ApplicationException e = Assertions.assertThrows(ApplicationException.class, () -> responseProcessor.getJsonEntity(uri, null, Object.class, clientResponse));

        verify(clientResponse, times(1)).readEntity(String.class);
        assertThat(e.getExceptionType()).isEqualTo(ExceptionType.REMOTE_SERVER_ERROR);
        assertThat(e.isAudited()).isEqualTo(false);
    }

    @Test
    void getJson_shouldThrowWhenResponseIsRequested() throws Exception {
        Response clientResponse = createMockResponse(200, "some entity");

        final ApplicationException e = Assertions.assertThrows(ApplicationException.class, () -> responseProcessor.getJsonEntity(uri, null, Response.class, clientResponse));

        assertThat(e.getExceptionType()).isEqualTo(ExceptionType.INVALID_CLIENTRESPONSE_PARAM);
    }

    @Test
    void getJson_shouldThrowWhenResponseGenericTypeIsRequested() throws Exception {
        Response clientResponse = createMockResponse(200, "some entity");

        final ApplicationException e = Assertions.assertThrows(ApplicationException.class, () -> responseProcessor.getJsonEntity(uri, new GenericType<Response>(){}, null, clientResponse));

        assertThat(e.getExceptionType()).isEqualTo(ExceptionType.INVALID_CLIENTRESPONSE_PARAM);
    }

    @Test
    void getJsonEntity_shouldCloseResponse() throws Exception {
        Response clientResponse = createMockResponse(200, "some entity");

        responseProcessor.getJsonEntity(uri, null, String.class, clientResponse);

        verify(clientResponse).close();
    }

    @Test
    void getJsonEntity_shouldReturnEmptyStringWhenNoClassNorGenericTypeSupplied() throws Exception {
        responseProcessor.getJsonEntity(uri, null, null, createMockResponse(200, "some entity"));
    }

    @Test
    void getJsonEntity_shouldThrowWhenNoEntityPresent() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> responseProcessor.getJsonEntity(uri, null, String.class, createMock204Response()));
    }

    @Test
    void getJsonEntity_shouldThrowWhenGettingEntityFails() {
        final ApplicationException e = Assertions.assertThrows(ApplicationException.class, () -> responseProcessor.getJsonEntity(uri, null, String.class, createResponseWithBadEntity()));

        assertThat(e.getExceptionType()).isEqualTo(ExceptionType.NETWORK_ERROR);
        assertThat(e.isAudited()).isEqualTo(false);
    }

    @SuppressWarnings("unchecked")
    private Response createResponseWithBadEntity() {
        int status = 200;
        Response clientResponse = mock(Response.class);
        when(clientResponse.readEntity(any(Class.class))).thenThrow(new ProcessingException("argh!"));
        when(clientResponse.hasEntity()).thenReturn(true);
        when(clientResponse.getStatus()).thenReturn(status);
        when(clientResponse.getStatusInfo()).thenReturn(Response.Status.fromStatusCode(status));
        return clientResponse;
    }

    private Response createMockResponse(int status, Object responseEntity) throws JsonProcessingException {
        Response clientResponse = mock(Response.class);
        ObjectMapper objectMapper = new ObjectMapper();
        when(clientResponse.readEntity(String.class)).thenReturn(objectMapper.writeValueAsString(responseEntity));
        when(clientResponse.readEntity(Response.class)).thenThrow(new RuntimeException("Can't deserialize json to Response"));
        when(clientResponse.hasEntity()).thenReturn(true);
        when(clientResponse.getStatus()).thenReturn(status);
        when(clientResponse.getStatusInfo()).thenReturn(Response.Status.fromStatusCode(status));
        return clientResponse;
    }

    private Response createMock204Response() throws JsonProcessingException {
        int status = 204;
        Response clientResponse = mock(Response.class);
        when(clientResponse.hasEntity()).thenReturn(false);
        when(clientResponse.getStatus()).thenReturn(status);
        when(clientResponse.getStatusInfo()).thenReturn(Response.Status.fromStatusCode(status));
        return clientResponse;
    }
}
