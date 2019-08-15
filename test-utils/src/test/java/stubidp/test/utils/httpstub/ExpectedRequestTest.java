package stubidp.test.utils.httpstub;

import com.google.common.collect.ImmutableMultimap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    public void shouldApplyIfPathIsNullInExpectedRequest() throws Exception {
        assertThat(new ExpectedRequest(null, null, null, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, never()).getPath();
        verify(receivedRequest, never()).getMethod();
        verify(receivedRequest, never()).getHeaders();
        verify(receivedRequest, never()).getEntityBytes();
    }

    @Test
    public void shouldApplyIfPathSetInExpectedRequestAndMatchesReceivedRequestPath() throws Exception {
        when(receivedRequest.getPath()).thenReturn("/some/path/to/foo");
        assertThat(new ExpectedRequest("/some/path/to/foo", null, null, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getPath();
    }

    @Test
    public void shouldNotApplyIfPathSetInExpectedRequestAndDoesntMatchesReceivedRequestPath() throws Exception {
        when(receivedRequest.getPath()).thenReturn("/some/path/to/foo");
        assertThat(new ExpectedRequest("/some/path/to/bar", null, null, null).applies(receivedRequest)).isFalse();
        verify(receivedRequest, times(1)).getPath();
    }

    @Test
    public void shouldApplyIfMethodSetInExpectedRequestAndMatchesReceivedRequestMethod() throws Exception {
        when(receivedRequest.getMethod()).thenReturn("GET");
        assertThat(new ExpectedRequest(null, "GET", null, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getMethod();
    }

    @Test
    public void shouldNotApplyIfMethodSetInExpectedRequestAndDoesntMatchesReceivedRequestMethod() throws Exception {
        when(receivedRequest.getMethod()).thenReturn("GET");
        assertThat(new ExpectedRequest(null, "POST", null, null).applies(receivedRequest)).isFalse();
        verify(receivedRequest, times(1)).getMethod();
    }

    @Test
    public void shouldApplyIfBodySetInExpectedRequestAndMatchesReceivedRequestEntity() throws Exception {
        when(receivedRequest.getEntityBytes()).thenReturn("any-body".getBytes());
        assertThat(new ExpectedRequest(null, null, null, "any-body").applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getEntityBytes();
    }

    @Test
    public void shouldNotApplyIfBodySetInExpectedRequestAndDoesntMatchesReceivedRequestEntity() throws Exception {
        when(receivedRequest.getEntityBytes()).thenReturn("any-body".getBytes());
        assertThat(new ExpectedRequest(null, null, null, "some-body").applies(receivedRequest)).isFalse();
        verify(receivedRequest, times(1)).getEntityBytes();
    }

    @Test
    public void shouldApplyIfHeadersSetInExpectedRequestAreAllFoundInReceivedRequestHeaders() throws Exception {
        when(receivedRequest.getHeaders()).thenReturn(ImmutableMultimap.<String, String>of("Key1", "Value1", "Key1", "Value3", "Key2", "Value2"));
        ImmutableMultimap<String, String> headers = ImmutableMultimap.of("Key1", "Value1", "Key2", "Value2", "Key1", "Value3");
        assertThat(new ExpectedRequest(null, null, headers, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getHeaders();
    }

    @Test
    public void shouldApplyIfHeadersSetInExpectedRequestAreEmpty() throws Exception {
        when(receivedRequest.getHeaders()).thenReturn(ImmutableMultimap.<String, String>of("Key1", "Value1", "Key1", "Value3", "Key2", "Value2"));
        ImmutableMultimap<String, String> headers = ImmutableMultimap.of();
        assertThat(new ExpectedRequest(null, null, headers, null).applies(receivedRequest)).isTrue();
        verify(receivedRequest, times(1)).getHeaders();
    }

    @Test
    public void shouldNotApplyIfHeadersSetInExpectedRequestAreNotAllFoundInReceivedRequestHeaders() throws Exception {
        when(receivedRequest.getHeaders()).thenReturn(ImmutableMultimap.<String, String>of("Key1", "Value1", "Key1", "Value3", "Key2", "Value2"));
        ImmutableMultimap<String, String> headers = ImmutableMultimap.of("Key1", "Value1", "Key2", "Value2", "Key1", "Value3", "Key3", "Value4");
        assertThat(new ExpectedRequest(null, null, headers, null).applies(receivedRequest)).isFalse();
        verify(receivedRequest, times(1)).getHeaders();
    }
}
