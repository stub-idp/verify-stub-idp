package stubidp.dropwizard.logstash;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.net.SyslogOutputStream;
import net.logstash.logback.layout.LogstashLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import stubidp.dropwizard.logstash.SyslogAppender;
import stubidp.dropwizard.logstash.SyslogEventFormatter;
import stubidp.dropwizard.logstash.UdpServer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static io.dropwizard.logging.SyslogAppenderFactory.Facility.LOCAL7;
import static org.assertj.core.api.Assertions.assertThat;

public class LogstashSyslogAppenderIntegrationTest {

    private final UdpServer udpServer = new UdpServer();

    @BeforeEach
    public void startUdpInterceptor() {
        udpServer.start();
    }

    @AfterEach
    public void stopUdpInteceptor() throws Exception {
        udpServer.stop();
    }

    @Test
    public void shouldSendLoggingEventToSyslogUdpSocket() throws Exception {
        String time = "2014-04-01T00:00:00.000Z";
        Clock clock = Clock.fixed(Instant.parse(time).atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

        SyslogOutputStream syslogOutputStream = new SyslogOutputStream("localhost", udpServer.getLocalPort());
        SyslogEventFormatter eventFormatter = new SyslogEventFormatter(LOCAL7, "source-host", "test-event-tag", new LogstashLayout(), clock);
        SyslogAppender appender = new SyslogAppender(eventFormatter, syslogOutputStream);

        appender.start();
        appender.append(new LoggingEvent("my.logger", (Logger) LoggerFactory.getLogger("my.logger"), Level.INFO, "message", null, null));

        assertThat(udpServer.getReceivedPacket()).startsWith("<189>1 2014-04-01T00:00:00.000Z source-host test-event-tag - - - {");
    }

}
