package stubidp.utils.rest.restclient;

import io.dropwizard.util.Duration;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeoutRequestRetryWithBackoffHandlerTest {

    private final HttpContext httpContext = new BasicHttpContext();

    @BeforeEach
    public void setup() {
        httpContext.setAttribute(HttpClientContext.HTTP_REQUEST, new BasicHttpRequest("GET", "http://localhost"));
    }

    @Test
    public void should_retry_ConnectTimeoutExceptionByDefault() {
        final int numRetries = 2;

        TimeoutRequestRetryWithBackoffHandler timeoutRequestRetryHandler = new TimeoutRequestRetryWithBackoffHandler(numRetries,Duration.milliseconds(100));
        final boolean expected = timeoutRequestRetryHandler.retryRequest(new ConnectTimeoutException(), 1, httpContext);

        assertThat(expected).isTrue();
    }

    @Test
    public void should_retry_SocketTimeoutExceptionByDefault() {
        final int numRetries = 2;

        TimeoutRequestRetryWithBackoffHandler timeoutRequestRetryHandler = new TimeoutRequestRetryWithBackoffHandler(numRetries,Duration.milliseconds(100));
        final boolean expected = timeoutRequestRetryHandler.retryRequest(new SocketTimeoutException(), 1, httpContext);

        assertThat(expected).isTrue();
    }

    @Test
    public void shouldRetryOnSpecifiedExceptionList() {
        final int numRetries = 2;
        final List<String> retryExceptionNames = Arrays.asList("org.apache.http.conn.ConnectTimeoutException", "java.net.SocketTimeoutException", "org.apache.http.NoHttpResponseException") ;

        TimeoutRequestRetryWithBackoffHandler timeoutRequestRetryHandler = new TimeoutRequestRetryWithBackoffHandler(numRetries,Duration.milliseconds(100),retryExceptionNames);
        boolean expected = timeoutRequestRetryHandler.retryRequest(new SocketTimeoutException(), 1, httpContext);
        assertThat(expected).isTrue();

        expected = timeoutRequestRetryHandler.retryRequest(new ConnectTimeoutException(), 1, httpContext);
        assertThat(expected).isTrue();

        expected = timeoutRequestRetryHandler.retryRequest(new NoHttpResponseException("Response is empty"), 1, httpContext);
        assertThat(expected).isTrue();

        expected = timeoutRequestRetryHandler.retryRequest(new IOException(), 1, httpContext);
        assertThat(expected).isFalse();
    }

    @Test
    public void should_only_retry_set_number_of_times() {
        final int numRetries = 2;
        final int executionCount = 3;

        TimeoutRequestRetryWithBackoffHandler timeoutRequestRetryHandler = new TimeoutRequestRetryWithBackoffHandler(numRetries,Duration.milliseconds(100));
        boolean expected = timeoutRequestRetryHandler.retryRequest(new ConnectTimeoutException(), numRetries, httpContext);

        assertThat(expected).isTrue();

        timeoutRequestRetryHandler = new TimeoutRequestRetryWithBackoffHandler(numRetries,Duration.milliseconds(100));
        expected = timeoutRequestRetryHandler.retryRequest(new ConnectTimeoutException(), executionCount, httpContext);

        assertThat(expected).isFalse();
    }

    @Test
    public void should_not_be_retry_other_exceptions() {
        final int numRetries = 2;

        TimeoutRequestRetryWithBackoffHandler timeoutRequestRetryHandler = new TimeoutRequestRetryWithBackoffHandler(numRetries,Duration.milliseconds(100));
        final boolean expected = timeoutRequestRetryHandler.retryRequest(new IOException(), 1, httpContext);

        assertThat(expected).isFalse();
    }

    @Test
    public void firstRetryShouldBackOffForSpecifiedPeriod() {
        final int numRetries = 3;
        final Duration backOffPeriod = Duration.milliseconds(100);
        final int retryAttempt = 1;

        TimeoutRequestRetryWithBackoffHandler timeoutRequestRetryHandler = new TimeoutRequestRetryWithBackoffHandler(numRetries,backOffPeriod);

        long start = System.currentTimeMillis();
        final boolean expected = timeoutRequestRetryHandler.retryRequest(new SocketTimeoutException(), retryAttempt, httpContext);
        long end = System.currentTimeMillis();

        assertThat(expected).isTrue();
        assertThat(retryAttempt * backOffPeriod.toMilliseconds()).isLessThanOrEqualTo(end-start);
    }

    @Test
    public void secondRetryShouldBackOffForTwiceSpecifiedPeriod() {
        final int numRetries = 3;
        final Duration backOffPeriod = Duration.milliseconds(100);
        final int retryAttempt = 2;

        TimeoutRequestRetryWithBackoffHandler timeoutRequestRetryHandler = new TimeoutRequestRetryWithBackoffHandler(numRetries,backOffPeriod);

        long start = System.currentTimeMillis();
        final boolean expected = timeoutRequestRetryHandler.retryRequest(new SocketTimeoutException(), retryAttempt, httpContext);
        long end = System.currentTimeMillis();

        assertThat(expected).isTrue();
        assertThat(retryAttempt * backOffPeriod.toMilliseconds()).isLessThanOrEqualTo(end-start);
    }

    @Test
    public void thirdRetryShouldBackOffForThriceSpecifiedPeriod() {
        final int numRetries = 3;
        final Duration backOffPeriod = Duration.milliseconds(100);
        final int retryAttempt = 3;

        TimeoutRequestRetryWithBackoffHandler timeoutRequestRetryHandler = new TimeoutRequestRetryWithBackoffHandler(numRetries,backOffPeriod);

        long start = System.currentTimeMillis();
        final boolean expected = timeoutRequestRetryHandler.retryRequest(new SocketTimeoutException(), retryAttempt, httpContext);
        long end = System.currentTimeMillis();

        assertThat(expected).isTrue();
        assertThat(retryAttempt * backOffPeriod.toMilliseconds()).isLessThanOrEqualTo(end-start);
    }
}
