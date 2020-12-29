package stubidp.utils.rest.jerseyclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.rest.exceptions.ApplicationException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ErrorHandlingClientTest {

    @Mock
    private Client client;

    @Mock
    private WebTarget webTarget;

    @Mock
    private Invocation.Builder webTargetBuilder;

    private ErrorHandlingClient errorHandlingClient;

    private final URI testUri = URI.create("/some-uri");

    @BeforeEach
    public void setup() {
        errorHandlingClient = new ErrorHandlingClient(client);
    }

    @Test
    public void getWithCookiesAndHeaders_shouldAddCookiesAndHeadersToRequest() {
        final Cookie cookie = new Cookie("cookie", "monster");
        final List<Cookie> cookies = List.of(cookie);
        final String headerName = "X-Clacks-Overhead";
        final String headerValue = "GNU Terry Pratchett";
        final Map<String, String> headers = Map.of(headerName, headerValue);

        when(client.target(any(URI.class))).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(webTargetBuilder);
        when(webTargetBuilder.accept(ArgumentMatchers.<MediaType>any())).thenReturn(webTargetBuilder);
        when(webTargetBuilder.cookie(ArgumentMatchers.any())).thenReturn(webTargetBuilder);
        when(webTargetBuilder.header(anyString(), any())).thenReturn(webTargetBuilder);

        errorHandlingClient.get(testUri, cookies, headers);

        verify(webTargetBuilder, times(1)).cookie(cookie);
        verify(webTargetBuilder, times(1)).header(headerName, headerValue);
        verify(webTargetBuilder, times(1)).get();
    }

    @Test
    public void get_shouldThrowApplicationExceptionWhenAWireProblemOccurs() {
        when(client.target(testUri)).thenThrow(new ProcessingException(""));

        Assertions.assertThrows(ApplicationException.class, () -> errorHandlingClient.get(testUri));
    }

    @Test
    public void postWithHeaders_shouldAddHeadersToRequest() {
        final String headerName = "X-Clacks-Overhead";
        final String headerValue = "GNU Terry Pratchett";
        final Map<String, String> headers = Map.of(headerName, headerValue);
        final String postBody = "";

        when(client.target(any(URI.class))).thenReturn(webTarget);
        when(webTarget.request(MediaType.APPLICATION_JSON_TYPE)).thenReturn(webTargetBuilder);
        when(webTargetBuilder.header(anyString(), any())).thenReturn(webTargetBuilder);

        errorHandlingClient.post(testUri, headers, postBody);

        verify(webTargetBuilder, times(1)).header(headerName, headerValue);
        verify(webTargetBuilder, times(1)).post(Entity.json(postBody));
    }

    @Test
    public void shouldRetryPostRequestIfConfigured() {
        when(client.target(any(URI.class))).thenReturn(webTarget);
        when(webTarget.request(MediaType.APPLICATION_JSON_TYPE)).thenReturn(webTargetBuilder);
        when(webTargetBuilder.post(Entity.json(""))).thenThrow(RuntimeException.class);

        ErrorHandlingClient retryEnabledErrorHandlingClient = new ErrorHandlingClient(client, 2);
        Assertions.assertThrows(ApplicationException.class, () -> retryEnabledErrorHandlingClient.post(testUri, Collections.emptyMap(), ""));

        verify(webTargetBuilder, times(3)).post(Entity.json(""));
    }

    @Test
    public void shouldRetryGetRequestIfConfigured() {
        when(client.target(any(URI.class))).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(webTargetBuilder);
        when(webTargetBuilder.accept(ArgumentMatchers.<MediaType>any())).thenReturn(webTargetBuilder);
        when(webTargetBuilder.get()).thenThrow(RuntimeException.class);

        ErrorHandlingClient retryEnabledErrorHandlingClient = new ErrorHandlingClient(client, 2);
        Assertions.assertThrows(ApplicationException.class, () -> retryEnabledErrorHandlingClient.get(testUri));

        verify(webTargetBuilder, times(3)).get();
    }

    @Test
    public void post_shouldThrowApplicationExceptionWhenAWireProblemOccurs() {
        when(client.target(testUri)).thenThrow(new ProcessingException(""));

        Assertions.assertThrows(ApplicationException.class, () -> errorHandlingClient.post(testUri, ""));
    }
}
