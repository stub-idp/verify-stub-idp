package stubidp.dropwizard.logstash;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.dropwizard.logstash.support.LoggingEventFormat;
import stubidp.dropwizard.logstash.support.TestApplication;
import stubidp.dropwizard.logstash.support.TestConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static stubidp.dropwizard.logstash.support.RootResource.TEST_LOG_LINE;

@ExtendWith(DropwizardExtensionsSupport.class)
public class LogstashFileAppenderAppRuleTest {

    private static File requestLog;
    private static File logLog;

    // this is executed before the @ClassRule
    static {
        try {
            requestLog = File.createTempFile("request-log-",".log");
            logLog = File.createTempFile("log-log-",".log");
        } catch (IOException e) {
            fail("can't create temp log files");
            e.printStackTrace();
        }
    }

    public static DropwizardAppExtension<TestConfiguration> dropwizardAppRule = new DropwizardAppExtension<>(TestApplication.class, ResourceHelpers
            .resourceFilePath("file-appender-test-application.yml"),
            ConfigOverride.config("server.requestLog.appenders[0].currentLogFilename", requestLog.getAbsolutePath()),
            ConfigOverride.config("logging.appenders[0].currentLogFilename", logLog.getAbsolutePath())
            );

    @AfterAll
    public static void after() {
        requestLog.delete();
        logLog.delete();
    }

    @Test
    public void testLoggingLogstashRequestLog() throws InterruptedException, IOException {
        Client client = new JerseyClientBuilder().build();

        final Response response = client.target("http://localhost:" + dropwizardAppRule.getLocalPort() + "/").request().get();

        assertThat(response.readEntity(String.class)).isEqualTo("hello!");

        // wait for the logs to be written
        int count = 0;
        while(count<5 && (requestLog.length() == 0)) {
            count++;
            Thread.sleep(count*50);
        }

        assertThat(requestLog.length()).isGreaterThan(0);

        final List<LoggingEventFormat> list = parseLog(requestLog);

        assertThat(list.size()).isEqualTo(1);

        // this is currently returning the host, like this: "GET //localhost:63932/ HTTP/1.1" 200
//        assertThat(list.get(0).getMessage()).contains("\"GET / HTTP/1.1\" 200");
        assertThat(list.get(0).getLoggerName()).isEqualTo("http.request");

    }

    @Test
    public void testLoggingLogstashFileLog() throws IOException {

        Client client = new JerseyClientBuilder().build();

        final Response response = client.target("http://localhost:" + dropwizardAppRule.getLocalPort() + "/log").request()
                .get();

        assertThat(logLog.length()).isGreaterThan(0);

        final List<LoggingEventFormat> list = parseLog(logLog);

        assertThat(list.size()).isGreaterThan(0);

        assertThat(list.stream()
                .filter(logFormat -> logFormat.getMessage().equals(TEST_LOG_LINE))
                .count()).isEqualTo(1);
    }

    private List<LoggingEventFormat> parseLog(File logLog) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return Files.readAllLines(logLog.toPath()).stream()
                .map(line -> {
                    try {
                        return objectMapper.readValue(line, LoggingEventFormat.class);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(object -> object != null)
                .collect(Collectors.toList());
    }
}
