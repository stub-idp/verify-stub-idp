package stubidp.utils.rest.restclient;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeoutRequestRetryHandlerTest {

    private final HttpContext httpContext = new BasicHttpContext();

    @BeforeEach
    public void setup() {
        httpContext.setAttribute(HttpClientContext.HTTP_REQUEST, new BasicHttpRequest("GET", "http://localhost"));
    }

    @Test
    public void should_retry_ConnectTimeoutException() {
        final int numRetries = 2;

        TimeoutRequestRetryHandler timeoutRequestRetryHandler = new TimeoutRequestRetryHandler(numRetries);
        final boolean expected = timeoutRequestRetryHandler.retryRequest(new ConnectTimeoutException(), 1, httpContext);

        assertThat(expected).isTrue();
    }

    @Test
    public void should_only_retry_set_number_of_times() {
        final int numRetries = 2;
        final int executionCount = 3;

        TimeoutRequestRetryHandler timeoutRequestRetryHandler = new TimeoutRequestRetryHandler(numRetries);
        boolean expected = timeoutRequestRetryHandler.retryRequest(new ConnectTimeoutException(), numRetries, httpContext);

        assertThat(expected).isTrue();

        expected = timeoutRequestRetryHandler.retryRequest(new ConnectTimeoutException(), executionCount, httpContext);

        assertThat(expected).isFalse();
    }

    @Test
    public void should_not_be_retry_other_exceptions() {
        final int numRetries = 2;

        TimeoutRequestRetryHandler timeoutRequestRetryHandler = new TimeoutRequestRetryHandler(numRetries);
        final boolean expected = timeoutRequestRetryHandler.retryRequest(new IOException(), 1, httpContext);

        assertThat(expected).isFalse();
    }

}
