package stubidp.utils.rest.jerseyclient;

import com.codahale.metrics.Meter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ProcessingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"all", "rawtypes"})
public class RetryCommandTest {

    private class DummyClass { <T> T function() { return null; } }

    @Test
    public void shouldRetryIfFirstAttemptFails() {
        RetryCommand<String> retryCommand = new RetryCommand<>(2);

        DummyClass dummy = mock(DummyClass.class);
        when(dummy.function()).thenThrow(RuntimeException.class).thenReturn("SUCCESS");

        String result = retryCommand.execute(dummy::function);

        assertThat(result).isEqualTo("SUCCESS");
        verify(dummy, times(2)).function();
    }

    @Test
    public void shouldNotRetryIfRetryCountIs0() {
        RetryCommand<String> retryCommand = new RetryCommand<>(0);

        DummyClass dummy = mock(DummyClass.class);
        when(dummy.function()).thenThrow(RuntimeException.class).thenReturn("SUCCESS");

        Assertions.assertThrows(ProcessingException.class, () -> retryCommand.execute(dummy::function));
    }

    @Test
    public void shouldNotRetryIfFirstRequestSucceeds() {
        RetryCommand<String> retryCommand = new RetryCommand<>(2);

        DummyClass dummy = mock(DummyClass.class);
        when(dummy.function()).thenReturn("SUCCESS");

        String result = retryCommand.execute(dummy::function);

        assertThat(result).isEqualTo("SUCCESS");
        verify(dummy, times(1)).function();
    }

    @Test
    public void shouldThrowProcessingExceptionIfMaxRetriesExceeded(){
        RetryCommand<String> retryCommand = new RetryCommand<>(2);

        DummyClass dummy = mock(DummyClass.class);
        when(dummy.function())
                .thenThrow(RuntimeException.class)
                .thenThrow(RuntimeException.class)
                .thenThrow(RuntimeException.class)
                .thenReturn("SUCCESS");

        Assertions.assertThrows(ProcessingException.class, () -> retryCommand.execute(dummy::function));
    }

    @Test
    public void shouldRetryOnSpecificExceptionIfSpecified() {
        RetryCommand<String> retryCommand = new RetryCommand<>(2, NotAuthorizedException.class);

        DummyClass dummy = mock(DummyClass.class);
        when(dummy.function())
                .thenThrow(NotAuthorizedException.class)
                .thenReturn("SUCCESS");

        String result = retryCommand.execute(dummy::function);

        assertThat(result).isEqualTo("SUCCESS");
        verify(dummy, times(2)).function();
    }

    @Test
    public void shouldThrowExceptionIfDoesNotMatchSpecifiedException() {
        RetryCommand<String> retryCommand = new RetryCommand<>(2, NotAuthorizedException.class);

        DummyClass dummy = mock(DummyClass.class);
        when(dummy.function())
                .thenThrow(NotAllowedException.class)
                .thenReturn("SUCCESS");

        Assertions.assertThrows(NotAllowedException.class, () -> retryCommand.execute(dummy::function));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldMarkRetryMeterWhenRetrying() {
        Meter retryMeter = mock(Meter.class);
        RetryCommand retryCommand = new RetryCommand(2, retryMeter);

        DummyClass dummy = mock(DummyClass.class);
        when(dummy.function())
                .thenThrow(NotAllowedException.class)
                .thenReturn("SUCCESS");

        retryCommand.execute(dummy::function);

        verify(retryMeter).mark();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotMarkRetryMeterWhenRetryingNotNeeded() {
        Meter retryMeter = mock(Meter.class);
        RetryCommand retryCommand = new RetryCommand(2, retryMeter);

        DummyClass dummy = mock(DummyClass.class);
        when(dummy.function()).thenReturn("SUCCESS");

        retryCommand.execute(dummy::function);

        verify(retryMeter, never()).mark();
    }

    @Test
    public void shouldNotErrorIfNoRetryMeterSpecified() {
        RetryCommand<String> retryCommand = new RetryCommand<>(2);

        DummyClass dummy = mock(DummyClass.class);
        when(dummy.function())
                .thenThrow(NotAllowedException.class)
                .thenReturn("SUCCESS");

        String result = retryCommand.execute(dummy::function);

        assertThat(result).isEqualTo("SUCCESS");
    }
}
