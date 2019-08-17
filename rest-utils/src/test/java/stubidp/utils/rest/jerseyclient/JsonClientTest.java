package stubidp.utils.rest.jerseyclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.rest.jerseyclient.ErrorHandlingClient;
import stubidp.utils.rest.jerseyclient.JsonClient;
import stubidp.utils.rest.jerseyclient.JsonResponseProcessor;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JsonClientTest {

    @Mock
    private ErrorHandlingClient errorHandlingClient;
    @Mock
    private JsonResponseProcessor jsonResponseProcessor;

    private JsonClient jsonClient;
    private URI testUri = URI.create("/some-uri");
    private String requestBody = "some-request-body";

    @BeforeEach
    public void setup() {
        jsonClient = new JsonClient(errorHandlingClient, jsonResponseProcessor);
    }

    @Test
    public void post_shouldDelegateToJsonResponseProcessorToCheckForErrors() throws Exception {
        Response clientResponse = Response.noContent().build();
        when(errorHandlingClient.post(testUri, requestBody)).thenReturn(clientResponse);

        jsonClient.post(requestBody, testUri);

        verify(jsonResponseProcessor, times(1)).getJsonEntity(testUri, null, null, clientResponse);
    }

    @Test
    public void basicPost_shouldDelegateToProcessor() throws Exception {
        Response clientResponse = Response.noContent().build();
        when(errorHandlingClient.post(testUri, requestBody)).thenReturn(clientResponse);

        jsonClient.post(requestBody, testUri, String.class);

        verify(jsonResponseProcessor, times(1)).getJsonEntity(testUri, null, String.class, clientResponse);
    }

    @Test
    public void basicGet_shouldDelegateToProcessor() throws Exception {
        Response clientResponse = Response.noContent().build();
        when(errorHandlingClient.get(testUri)).thenReturn(clientResponse);

        jsonClient.get(testUri, String.class);

        verify(jsonResponseProcessor, times(1)).getJsonEntity(testUri, null, String.class, clientResponse);
    }

    @Test
    public void getWithGenericType_shouldDelegateToProcessor() throws Exception {
        Response clientResponse = Response.noContent().build();
        when(errorHandlingClient.get(testUri)).thenReturn(clientResponse);
        GenericType<String> genericType = new GenericType<>() {};

        jsonClient.get(testUri, genericType);

        verify(jsonResponseProcessor, times(1)).getJsonEntity(testUri, genericType, null, clientResponse);
    }
}
