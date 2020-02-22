package stubidp.dropwizard.logstash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import junit5.extensions.CaptureSystemOutput;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.dropwizard.logstash.support.AccessEventFormat;
import stubidp.dropwizard.logstash.support.LoggingEventFormat;
import stubidp.dropwizard.logstash.support.TestApplication;
import stubidp.dropwizard.logstash.support.TestConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.dropwizard.logstash.support.RootResource.TEST_LOG_LINE;

@ExtendWith(DropwizardExtensionsSupport.class)
public class LogstashConsoleAppenderAppRuleTest {

    public static DropwizardAppExtension<TestConfiguration> dropwizardAppRule = new DropwizardAppExtension<>(TestApplication.class, ResourceHelpers.resourceFilePath("console-appender-test-application.yml"));

    @Test
    @CaptureSystemOutput
    public void testLoggingLogstashRequestLog(CaptureSystemOutput.OutputCapture outputCapture) throws InterruptedException, IOException {
        Client client = new JerseyClientBuilder().build();

        for(int i=0;i<5;i++) {
            final Response response = client.target("http://localhost:" + dropwizardAppRule.getLocalPort() + "/?queryparam=test").request()
                    .header("Referer", "http://foobar/").header("User-Agent", "lynx/1.337").get();
            assertThat(response.readEntity(String.class)).isEqualTo("hello!");
        }
        // If we try to read systemOutRule too quickly, under some circumstances the appender won't have
        // successfully written the expected access log line yet.  We haven't got to the bottom of this
        // but it seems to depend on whether another DropwizardAppRule test has been run before this one.
        // sleeping for a while fixes the problem
        Thread.sleep(500);

        final List<AccessEventFormat> list = parseLogsOfType(AccessEventFormat.class, outputCapture);

        List<AccessEventFormat> accessEventStream = list.stream().filter(accessLog -> accessLog.getMethod().equals("GET")).collect(toList());
        assertThat(accessEventStream.size()).as("check there's an access log in the following:\n%s", List.of(outputCapture.toString().split(System.lineSeparator()))).isGreaterThanOrEqualTo(1);
        AccessEventFormat accessEvent = accessEventStream.get(0);
        assertThat(accessEvent.getMethod()).isEqualTo("GET");
        assertThat(accessEvent.getReferer()).isEqualTo("http://foobar/");
        assertThat(accessEvent.getUserAgent()).isEqualTo("lynx/1.337");
        assertThat(accessEvent.getHost()).startsWith("localhost");
        assertThat(accessEvent.getBytesSent()).isEqualTo("hello!".length());
        assertThat(accessEvent.getUrl()).isEqualTo("/?queryparam=test");
        assertThat(accessEvent.getHttpVersion()).isEqualTo("1.1");
        assertThat(accessEvent.getResponseCode()).isEqualTo(200);
        assertThat(accessEvent.getRemoteIp()).isEqualTo("127.0.0.1");
        assertThat(accessEvent.getVersion()).isEqualTo(1);
        // ballpark check that the unit is in the right order of magnitude
        // this test should hopefully catch a value that's erroneously measured in seconds
        assertThat(accessEvent.getElapsedTimeMillis()).isBetween(3,3000);
    }

    @Test
    @CaptureSystemOutput
    public void testRequestLogWithMissingRefererHeader(CaptureSystemOutput.OutputCapture outputCapture) throws InterruptedException, IOException {
        Client client = new JerseyClientBuilder().build();

        for(int i=0;i<5;i++) {
            final Response response = client.target("http://localhost:" + dropwizardAppRule.getLocalPort() + "/").request()
                    .get();
            assertThat(response.readEntity(String.class)).isEqualTo("hello!");
        }

        // If we try to read systemOutRule too quickly, under some circumstances the appender won't have
        // successfully written the expected access log line yet.  We haven't got to the bottom of this
        // but it seems to depend on whether another DropwizardAppRule test has been run before this one.
        // sleeping for a while fixes the problem
        Thread.sleep(500);

        final List<AccessEventFormat> list = parseLogsOfType(AccessEventFormat.class, outputCapture);

        List<AccessEventFormat> accessEventStream = list.stream().filter(accessLog -> accessLog.getMethod().equals("GET")).collect(toList());
        assertThat(accessEventStream.size()).as("check there's an access log in the following:\n%s", List.of(outputCapture.toString().split(System.lineSeparator()))).isGreaterThanOrEqualTo(1);
        AccessEventFormat accessEvent = accessEventStream.get(0);
        assertThat(accessEvent.getReferer()).isEqualTo("-");
    }


    @Test
    @CaptureSystemOutput
    public void testLoggingLogstashFileLog(CaptureSystemOutput.OutputCapture outputCapture) throws IOException, InterruptedException {

        Client client = new JerseyClientBuilder().build();

        final Response response = client.target("http://localhost:" + dropwizardAppRule.getLocalPort() + "/log").request()
                .get();

        final List<LoggingEventFormat> list = parseLogsOfType(LoggingEventFormat.class, outputCapture);

        assertThat(list.size()).isEqualTo(1);

        assertThat(list.stream()
                .filter(logFormat -> logFormat.getMessage().equals(TEST_LOG_LINE))
                .count()).isEqualTo(1);
    }

    private <E> List<E> parseLogsOfType(Class<E> logType, CaptureSystemOutput.OutputCapture outputCapture) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<E> list = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(outputCapture.toString(), System.lineSeparator());
        while (stringTokenizer.hasMoreTokens()) {
            try {
                E line = objectMapper.readValue(stringTokenizer.nextToken(), logType);
                list.add(line);
            } catch (JsonProcessingException e) {
                // it's not a log of type `logType`, ignore it
            }
        }
        return list;
    }
}
