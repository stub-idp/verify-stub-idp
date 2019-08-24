package stubidp.dropwizard.logstash;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;
import io.dropwizard.logging.SyslogAppenderFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SyslogEventFormatterTest {

    public SyslogEventFormatter formatter;
    private String hostname = "test-hostname";
    private String tag = "test-tag";

    @Mock
    private Layout<ILoggingEvent> layout;

    @BeforeEach
    public void setUp() throws Exception {
        formatter = new SyslogEventFormatter(SyslogAppenderFactory.Facility.LOCAL1, hostname, tag, layout);
    }

    @Test
    public void format_shouldPrefixWithExpectedPriorityWhenFacilityIsLocal1AndEventSeverityIsWarning() throws Exception {
        final LoggingEvent loggingEvent = createLoggingEvent(Level.WARN);

        final String formattedEvent = formatter.format(loggingEvent);

        assertThat(formattedEvent).startsWith("<141>");
    }

    @Test
    public void format_shouldIncludeTimestampInIso8601Format() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(DateTime.parse("2009-06-15T13:45:30.000Z").getMillis());
        final ILoggingEvent event = createLoggingEvent();

        final String formattedEvent = formatter.format(event);

        assertThat(formattedEvent).contains("2009-06-15T13:45:30.000Z");
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void format_shouldIncludeHostname() throws Exception {
        final String formattedEvent = formatter.format(createLoggingEvent());

        assertThat(formattedEvent).contains(hostname);
    }

    @Test
    public void format_shouldIncludeTag() throws Exception {
        final String formattedEvent = formatter.format(createLoggingEvent());

        assertThat(formattedEvent).contains(tag);
    }

    @Test
    public void format_shouldIncludeEventAsJson() throws Exception {
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
        event.setCallerData(new StackTraceElement[]{});
        return event;
    }
}
