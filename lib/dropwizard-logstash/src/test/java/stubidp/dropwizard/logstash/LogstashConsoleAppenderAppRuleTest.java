package stubidp.dropwizard.logstash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.dropwizard.logstash.RootResource.HELLO_MESSAGE;
import static stubidp.dropwizard.logstash.RootResource.TEST_LOG_LINE;

@ExtendWith(DropwizardExtensionsSupport.class)
public class LogstashConsoleAppenderAppRuleTest {

    public static final DropwizardAppExtension<TestConfiguration> dropwizardAppRule = new DropwizardAppExtension<>(TestApplication.class, ResourceHelpers.resourceFilePath("console-appender-test-application.yml"));

    private final Client client = new JerseyClientBuilder().build();

    @Test
    @CaptureSystemOutput
    public void testLoggingLogstashRequestLog(CaptureSystemOutput.OutputCapture outputCapture) {
        final String REFERER = "http://foobar/";
        final String USER_AGENT = "lynx/1.337";
        final String URL_PATH = "/?queryparam=test";

        for(int i = 0; i<5; i++) {
            final Response response = client.target("http://localhost:" + dropwizardAppRule.getLocalPort() + URL_PATH)
                    .request()
                    .header("Referer", REFERER)
                    .header("User-Agent", USER_AGENT)
                    .get();
            assertThat(response.readEntity(String.class)).isEqualTo(HELLO_MESSAGE);
        }

        final List<AccessEventFormat> list = parseLogsOfType(AccessEventFormat.class, outputCapture);

        List<AccessEventFormat> accessEventStream = list.stream().filter(accessLog -> accessLog.getMethod().equals("GET")).collect(toList());
        assertThat(accessEventStream.size()).as("check there's an access log in the following:\n%s", List.of(outputCapture.toString().split(System.lineSeparator()))).isGreaterThanOrEqualTo(1);
        AccessEventFormat accessEvent = accessEventStream.get(0);
        assertThat(accessEvent.getMethod()).isEqualTo("GET");
        assertThat(accessEvent.getReferer()).isEqualTo(REFERER);
        assertThat(accessEvent.getUserAgent()).isEqualTo(USER_AGENT);
        assertThat(accessEvent.getHost()).startsWith("localhost");
        assertThat(accessEvent.getBytesSent()).isEqualTo(HELLO_MESSAGE.length());
        assertThat(accessEvent.getUrl()).isEqualTo(URL_PATH);
        assertThat(accessEvent.getHttpVersion()).isEqualTo("1.1");
        assertThat(accessEvent.getResponseCode()).isEqualTo(200);
        assertThat(accessEvent.getRemoteIp()).isEqualTo("127.0.0.1");
        assertThat(accessEvent.getVersion()).isEqualTo(1);
        // ballpark check that the unit is in the right order of magnitude
        // this test should hopefully catch a value that's erroneously measured in seconds
        assertThat(accessEvent.getElapsedTimeMillis()).isBetween(1,4000);
    }

    @Test
    @CaptureSystemOutput
    public void testRequestLogWithMissingRefererHeader(CaptureSystemOutput.OutputCapture outputCapture) {
        for(int i=0;i<5;i++) {
            final Response response = client.target("http://localhost:" + dropwizardAppRule.getLocalPort() + "/")
                    .request()
                    .get();
            assertThat(response.readEntity(String.class)).isEqualTo("hello!");
        }

        final List<AccessEventFormat> list = parseLogsOfType(AccessEventFormat.class, outputCapture);

        List<AccessEventFormat> accessEventStream = list.stream().filter(accessLog -> accessLog.getMethod().equals("GET")).collect(toList());
        assertThat(accessEventStream.size()).as("check there's an access log in the following:\n%s", List.of(outputCapture.toString().split(System.lineSeparator()))).isGreaterThanOrEqualTo(1);
        AccessEventFormat accessEvent = accessEventStream.get(0);
        assertThat(accessEvent.getReferer()).isEqualTo("-");
    }

    @Test
    @CaptureSystemOutput
    public void testLoggingLogstashFileLog(CaptureSystemOutput.OutputCapture outputCapture) {
        for(int i=0;i<5;i++) {
            final Response response = client.target("http://localhost:" + dropwizardAppRule.getLocalPort() + "/log")
                    .request()
                    .get();
            assertThat(response.getStatus()).isEqualTo(204);
        }

        final List<LoggingEventFormat> list = parseLogsOfType(LoggingEventFormat.class, outputCapture);

        assertThat(list.size()).isGreaterThanOrEqualTo(1);
        assertThat(list.stream()
                .filter(logFormat -> logFormat.getMessage().equals(TEST_LOG_LINE))
                .count()).isGreaterThanOrEqualTo(1);
    }

    private <E> List<E> parseLogsOfType(Class<E> logType, CaptureSystemOutput.OutputCapture outputCapture) {
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
