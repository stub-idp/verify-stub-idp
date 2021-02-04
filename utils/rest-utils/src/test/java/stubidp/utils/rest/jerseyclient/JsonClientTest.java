package stubidp.utils.rest.jerseyclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JsonClientTest {

    @Mock
    private ErrorHandlingClient errorHandlingClient;
    @Mock
    private JsonResponseProcessor jsonResponseProcessor;

    private JsonClient jsonClient;
    private final URI testUri = URI.create("/some-uri");
    private final String requestBody = "some-request-body";

    @BeforeEach
    void setup() {
        jsonClient = new JsonClient(errorHandlingClient, jsonResponseProcessor);
    }

    @Test
    void post_shouldDelegateToJsonResponseProcessorToCheckForErrors() {
        Response clientResponse = Response.noContent().build();
        when(errorHandlingClient.post(testUri, requestBody)).thenReturn(clientResponse);

        jsonClient.post(requestBody, testUri);

        verify(jsonResponseProcessor, times(1)).getJsonEntity(testUri, null, null, clientResponse);
    }

    @Test
    void basicPost_shouldDelegateToProcessor() {
        Response clientResponse = Response.noContent().build();
        when(errorHandlingClient.post(testUri, requestBody)).thenReturn(clientResponse);

        jsonClient.post(requestBody, testUri, String.class);

        verify(jsonResponseProcessor, times(1)).getJsonEntity(testUri, null, String.class, clientResponse);
    }

    @Test
    void basicGet_shouldDelegateToProcessor() {
        Response clientResponse = Response.noContent().build();
        when(errorHandlingClient.get(testUri)).thenReturn(clientResponse);

        jsonClient.get(testUri, String.class);

        verify(jsonResponseProcessor, times(1)).getJsonEntity(testUri, null, String.class, clientResponse);
    }

    @Test
    void getWithHeadersShouldPassHeadersToErrorHandlingClient() {
        String headerName = "X-Sausages";
        String headerValue = "Yes please";
        final Map<String, String> headers = Map.of(headerName, headerValue);

        jsonClient.get(testUri, String.class, headers);

        verify(errorHandlingClient).get(testUri, headers);
    }

    @Test
    void getWithGenericType_shouldDelegateToProcessor() {
        Response clientResponse = Response.noContent().build();
        when(errorHandlingClient.get(testUri)).thenReturn(clientResponse);
        GenericType<String> genericType = new GenericType<>() {};

        jsonClient.get(testUri, genericType);

        verify(jsonResponseProcessor, times(1)).getJsonEntity(testUri, genericType, null, clientResponse);
    }
}
