package stubidp.eventemitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.eventemitter.Event;
import stubidp.eventemitter.EventEmitter;
import stubidp.eventemitter.EventEncrypter;
import stubidp.eventemitter.EventEncryptionException;
import stubidp.eventemitter.EventHasher;
import stubidp.eventemitter.EventSender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.eventemitter.EventMessageBuilder.anEventMessage;

@ExtendWith(MockitoExtension.class)
public class EventEmitterTest {

    private static final String ENCRYPTED_EVENT = "encrypted event";

    private EventEmitter eventEmitter;
    private final Event event = anEventMessage().build();

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventHasher eventHasher;

    @Mock
    private EventEncrypter eventEncrypter;

    @Mock
    private EventSender eventSender;

    @BeforeEach
    public void setUp() throws Exception {
        eventEmitter = new EventEmitter(objectMapper, eventHasher, eventEncrypter, eventSender);
    }

    @Test
    public void shouldEncryptAndSendEncryptedEventToSqs() throws Exception {
        when(eventHasher.replacePersistentIdWithHashedPersistentId(event)).thenReturn(event);
        when(eventEncrypter.encrypt(event)).thenReturn(ENCRYPTED_EVENT);

        eventEmitter.record(event);

        verify(objectMapper).writeValueAsString(event);
        verify(eventHasher).replacePersistentIdWithHashedPersistentId(event);
        verify(eventEncrypter).encrypt(event);
        verify(eventSender).sendAuthenticated(event, ENCRYPTED_EVENT);
    }

    @Test
    public void shouldLogErrorAfterFailingToEncrypt() throws Exception {
        when(eventHasher.replacePersistentIdWithHashedPersistentId(event)).thenReturn(event);
        final String errorMessage = "Failed to encrypt.";
        when(eventEncrypter.encrypt(event)).thenThrow(new EventEncryptionException(String.format(
                "Failed to send a message [Event Id: %s] to the queue. Error Message: %s\nEvent Message: null\n",
                event.getEventId().toString(),
                errorMessage)));

        try (ByteArrayOutputStream errorContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(errorContent)) {
            System.setOut(printStream);
            eventEmitter.record(event);
            System.setOut(System.out);

            assertThat(errorContent.toString()).contains(String.format(
                    "Failed to send a message [Event Id: %s] to the queue. Error Message: %s\nEvent Message: null\n",
                    event.getEventId().toString(),
                    errorMessage
            ));
        }
    }

    @Test
    public void shouldLogErrorWhenEventIsNull() throws IOException {
        try (ByteArrayOutputStream errorContent = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(errorContent)) {
            System.setOut(printStream);
            eventEmitter.record(null);
            System.setOut(System.out);

            assertThat(errorContent.toString()).contains("Unable to send a message due to event containing null value.\n");
        }
    }
}
