package stubidp.utils.common.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.text.MessageFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LevelLoggerTest {

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    //Captor is genericised with ch.qos.logback.classic.spi.LoggingEvent
    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    private LevelLogger<LevelLoggerTest> levelLogger = LevelLogger.getLevelLogger(LevelLoggerTest.class);
    private UUID errorId = UUID.randomUUID();
    private final String innerErrorMessage = "innerErrorMessage";

    @BeforeEach
    public void setUp() throws Exception {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @Test
    public void assertExceptionIsLoggedWithLevelError() {
        levelLogger.log(Level.ERROR, new RuntimeException(innerErrorMessage), errorId);
        String message = MessageFormat.format("UNEXPECTED_EXCEPTION – '{'id: {0}, message: {1}'}'", errorId, innerErrorMessage);
        verifyLogMessageAndLevel(ch.qos.logback.classic.Level.ERROR, message);
    }

    @Test
    public void assertExceptionIsLoggedWithLevelWarn() {
        levelLogger.log(Level.WARN, new RuntimeException(innerErrorMessage), errorId);
        String message = MessageFormat.format("UNEXPECTED_EXCEPTION – '{'id: {0}, message: {1}'}'", errorId, innerErrorMessage);
        verifyLogMessageAndLevel(ch.qos.logback.classic.Level.WARN, message);
    }

    @Test
    public void assertExceptionIsLoggedWithLevelInfo() {
        levelLogger.log(Level.INFO, new RuntimeException(innerErrorMessage), errorId);
        String message = MessageFormat.format("UNEXPECTED_EXCEPTION – '{'id: {0}, message: {1}'}'", errorId, innerErrorMessage);
        verifyLogMessageAndLevel(ch.qos.logback.classic.Level.INFO, message);
    }

    @Test
    public void assertExceptionIsLoggedWithLevelDebug() {
        levelLogger.log(Level.DEBUG, new RuntimeException(innerErrorMessage), errorId);
        String message = MessageFormat.format("UNEXPECTED_EXCEPTION – '{'id: {0}, message: {1}'}'", errorId, innerErrorMessage);
        verifyLogMessageAndLevel(ch.qos.logback.classic.Level.DEBUG, message);
    }

    private void verifyLogMessageAndLevel(ch.qos.logback.classic.Level level, String message) {
        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        assertThat(loggingEvent.getLevel()).isEqualTo(level);
        assertThat(loggingEvent.getMessage()).isEqualTo(message);
    }
}
