package stubidp.test.utils.httpstub;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpectedRequestTest {

    @Mock
    private RecordedRequest receivedRequest;

    @Test
    void shouldApplyIfPathIsNullInExpectedRequest() {
        assertThat(new ExpectedRequest(null, null, null, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, never()).getPath();
        verify(receivedRequest, never()).getMethod();
        verify(receivedRequest, never()).getHeaders();
        verify(receivedRequest, never()).getEntityBytes();
    }

    @Test
    void shouldApplyIfPathSetInExpectedRequestAndMatchesReceivedRequestPath() {
        when(receivedRequest.getPath()).thenReturn("/some/path/to/foo");
        assertThat(new ExpectedRequest("/some/path/to/foo", null, null, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getPath();
    }

    @Test
    void shouldNotApplyIfPathSetInExpectedRequestAndDoesntMatchesReceivedRequestPath() {
        when(receivedRequest.getPath()).thenReturn("/some/path/to/foo");
        assertThat(new ExpectedRequest("/some/path/to/bar", null, null, null).applies(receivedRequest)).isFalse();
        verify(receivedRequest, times(1)).getPath();
    }

    @Test
    void shouldApplyIfMethodSetInExpectedRequestAndMatchesReceivedRequestMethod() {
        when(receivedRequest.getMethod()).thenReturn("GET");
        assertThat(new ExpectedRequest(null, "GET", null, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getMethod();
    }

    @Test
    void shouldNotApplyIfMethodSetInExpectedRequestAndDoesntMatchesReceivedRequestMethod() {
        when(receivedRequest.getMethod()).thenReturn("GET");
        assertThat(new ExpectedRequest(null, "POST", null, null).applies(receivedRequest)).isFalse();
        verify(receivedRequest, times(1)).getMethod();
    }

    @Test
    void shouldApplyIfBodySetInExpectedRequestAndMatchesReceivedRequestEntity() {
        when(receivedRequest.getEntityBytes()).thenReturn("any-body".getBytes());
        assertThat(new ExpectedRequest(null, null, null, "any-body").applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getEntityBytes();
    }

    @Test
    void shouldNotApplyIfBodySetInExpectedRequestAndDoesntMatchesReceivedRequestEntity() {
        when(receivedRequest.getEntityBytes()).thenReturn("any-body".getBytes());
        assertThat(new ExpectedRequest(null, null, null, "some-body").applies(receivedRequest)).isFalse();
        verify(receivedRequest, times(1)).getEntityBytes();
    }

    @Test
    void shouldApplyIfHeadersSetInExpectedRequestAreAllFoundInReceivedRequestHeaders() {
        final Map<String, List<String>> sentHeaders = Map.of("Key1", List.of("Value1", "Value3"), "Key2", List.of("Value2"));
        when(receivedRequest.getHeaders()).thenReturn(sentHeaders);
        Map<String, List<String>> requiredHeaders = Map.of("Key2", List.of("Value2"), "Key1", List.of("Value1", "Value3"));
        assertThat(new ExpectedRequest(null, null, requiredHeaders, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getHeaders();
    }

    @Test
    void shouldApplyIfHeadersSetInExpectedRequestAreEmpty() {
        final Map<String, List<String>> sentHeaders = Map.of("Key1", List.of("Value1", "Value3"), "Key2", List.of("Value2"));
        when(receivedRequest.getHeaders()).thenReturn(sentHeaders);
        Map<String, List<String>> requiredHeaders = Map.of();
        assertThat(new ExpectedRequest(null, null, requiredHeaders, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getHeaders();
    }

    @Test
    void shouldNotApplyIfHeadersSetInExpectedRequestAreNotAllFoundInReceivedRequestHeaders() {
        final Map<String, List<String>> sentHeaders = Map.of("Key1", List.of("Value1", "Value3"), "Key2", List.of("Value2"));
        when(receivedRequest.getHeaders()).thenReturn(sentHeaders);
        Map<String, List<String>> requiredHeaders = Map.of("Key2", List.of("Value2"), "Key1", List.of("Value1", "Value3"), "Key3", List.of("Value4"));
        assertThat(new ExpectedRequest(null, null, requiredHeaders, null).applies(receivedRequest)).isFalse();
        verify(receivedRequest, times(1)).getHeaders();
    }
}
