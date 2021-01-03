package stubidp.dropwizard.logstash;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;
import io.dropwizard.logging.SyslogAppenderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.dropwizard.logstash.SyslogEventFormatter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SyslogEventFormatterTest {

    private static final StackTraceElement[] STACK_TRACE_ELEMENTS = {};

    private SyslogEventFormatter formatter;
    private final String hostname = "test-hostname";
    private final String tag = "test-tag";

    @Mock
    private Layout<ILoggingEvent> layout;

    @BeforeEach
    void setUp() {
        formatter = new SyslogEventFormatter(SyslogAppenderFactory.Facility.LOCAL1, hostname, tag, layout);
    }

    @Test
    void format_shouldPrefixWithExpectedPriorityWhenFacilityIsLocal1AndEventSeverityIsWarning() {
        final LoggingEvent loggingEvent = createLoggingEvent(Level.WARN);

        final String formattedEvent = formatter.format(loggingEvent);

        assertThat(formattedEvent).startsWith("<141>");
    }

    @Test
    void format_shouldIncludeTimestampInIso8601Format() {
        final String time = "2009-06-15T13:45:30.000Z";
        final Clock clock = Clock.fixed(Instant.parse(time).atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        final ILoggingEvent event = createLoggingEvent();

        formatter = new SyslogEventFormatter(SyslogAppenderFactory.Facility.LOCAL1, hostname, tag, layout, clock);
        final String formattedEvent = formatter.format(event);

        assertThat(formattedEvent).contains(time);
    }

    @Test
    void format_shouldIncludeHostname() {
        final String formattedEvent = formatter.format(createLoggingEvent());

        assertThat(formattedEvent).contains(hostname);
    }

    @Test
    void format_shouldIncludeTag() {
        final String formattedEvent = formatter.format(createLoggingEvent());

        assertThat(formattedEvent).contains(tag);
    }

    @Test
    void format_shouldIncludeEventAsJson() {
        final ILoggingEvent event = createLoggingEvent();
        when(layout.doLayout(event)).thenReturn("formatted event");

        final String formattedEvent = formatter.format(event);

        assertThat(formattedEvent).contains("formatted event");
    }

    private ILoggingEvent createLoggingEvent() {
        return createLoggingEvent(Level.DEBUG);
    }

    private LoggingEvent createLoggingEvent(Level level) {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(level);
        event.setCallerData(STACK_TRACE_ELEMENTS);
        return event;
    }
}
