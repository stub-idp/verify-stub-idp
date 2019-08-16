package stubidp.eventemitter;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import org.assertj.core.api.Fail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import stubidp.eventemitter.utils.HttpResponse;
import stubidp.test.utils.httpstub.ExpectedRequest;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.httpstub.RegisteredResponse;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static stubidp.eventemitter.EventMessageBuilder.anEventMessage;

@Disabled("takes 60s to run")
public class EventEmitterAmazonEventSenderTest {

    private static final String DUMMY_AWS_ACCESS_KEY = "ABCD1234EFGH5678IJKL";
    private static final String DUMMY_AWS_SECRET_ACCESS_KEY = "abcd1234DEFH5678ijkl4321mnop8765qrst1234";

    private static final ExpectedRequest expectedRequest = new ExpectedRequest(EventEmitterTestHelper.AUDIT_EVENTS_API_RESOURCE, "POST", null, null);

    @RegisterExtension
    public static HttpStubRule apiGatewayStub = new HttpStubRule();

    @BeforeEach
    public void setup() {
        apiGatewayStub.reset();
    }

    @AfterEach
    public void tearDown() {
        apiGatewayStub.reset();
    }

    @Test
    public void shouldReturnHTTP403WhenCredentialsMissing() throws Exception {

        final RegisteredResponse expectedResponse = new RegisteredResponse(
                HttpResponse.HTTP_403.getStatusCode(),
                "application/json",
                "hello",
                EventEmitterTestHelper.createTestResponseHeadersMap()
        );

        final EventSender amazonEventSender =
                new AmazonEventSender(URI.create(apiGatewayStub.baseUri().build().toString() + EventEmitterTestHelper.AUDIT_EVENTS_API_RESOURCE),
                        new BasicAWSCredentials("", ""),
                        Regions.EU_WEST_2);


        try {
            apiGatewayStub.register(expectedRequest, expectedResponse);
            amazonEventSender.sendAuthenticated(anEventMessage().build(), "encryptedEvent");
            Fail.fail("Should return Forbidden (403)");
        } catch (AwsResponseException e) {
            assertThat(e.getResponse().getStatusCode()).isEqualTo(HttpResponse.HTTP_403.getStatusCode());
        }
    }

    @Test
    public void shouldReturnHTTP200WhenAuthenticationSucceeds() throws Exception {

        final RegisteredResponse expectedResponse = new RegisteredResponse(
                HttpResponse.HTTP_200.getStatusCode(),
                "application/json",
                "hello",
                EventEmitterTestHelper.createTestResponseHeadersMap()
        );
        final EventSender amazonEventSender =
                new AmazonEventSender(URI.create(apiGatewayStub.baseUri().build().toString() + EventEmitterTestHelper.AUDIT_EVENTS_API_RESOURCE),
                        new BasicAWSCredentials(DUMMY_AWS_ACCESS_KEY, DUMMY_AWS_SECRET_ACCESS_KEY),
                        Regions.EU_WEST_2);

        /*
            Expected headers not provided as HttpStub requires both header names and content to match, and content is dynamic.
            The expected headers are tested separated using assertions in the try block below.
         */
        apiGatewayStub.register(expectedRequest, expectedResponse);
        amazonEventSender.sendAuthenticated(anEventMessage().build(), "encryptedEvent");

        /*
            Check that all the headers required for AWS Authentication and AWS4 Signing are present.
            Header contents are not validated.
        */
        assertThat(apiGatewayStub.getLastRequest().getHeader("Host")).isNotNull();
        assertThat(apiGatewayStub.getLastRequest().getHeader("Authorization")).isNotNull();

        // Authorization header must contain all of the following keywords...
        assertThat(apiGatewayStub.getLastRequest().getHeader("Authorization")).containsPattern(Pattern.compile("(?=.*AWS4-HMAC-SHA256)(?=.*Signature)(?=.*Credential)"));
        assertThat(apiGatewayStub.getLastRequest().getHeader("Content-type")).isEqualToIgnoringCase("application/json");
        assertThat(apiGatewayStub.getLastRequest().getHeader("X-Amz-Date")).isNotNull();

    }

    @Test
    public void shouldRetryRequests() throws UnsupportedEncodingException {
        final RegisteredResponse errorResponse = new RegisteredResponse(
                HttpResponse.HTTP_504.getStatusCode(),
                "application/json",
                "error",
                EventEmitterTestHelper.createTestResponseHeadersMap()
        );
        final EventSender amazonEventSender =
                new AmazonEventSender(URI.create(apiGatewayStub.baseUri().build().toString() + EventEmitterTestHelper.AUDIT_EVENTS_API_RESOURCE),
                        new BasicAWSCredentials(DUMMY_AWS_ACCESS_KEY, DUMMY_AWS_SECRET_ACCESS_KEY),
                        Regions.EU_WEST_2);

        apiGatewayStub.register(expectedRequest, errorResponse);
        try {
            amazonEventSender.sendAuthenticated(anEventMessage().build(), "encryptedEvent");
        } catch (Exception e) {
            assertThat(apiGatewayStub.getCountOfRequests()).isGreaterThan(1);
        }
    }

    @Test
    public void shouldThrowAwsResponseExceptioWhenNotFound() throws UnsupportedEncodingException {

        apiGatewayStub.register(EventEmitterTestHelper.AUDIT_EVENTS_API_RESOURCE, HttpResponse.HTTP_403.getStatusCode());
        final EventSender amazonEventSender =
                new AmazonEventSender(URI.create(apiGatewayStub.baseUri().build().toString()),
                        new BasicAWSCredentials(DUMMY_AWS_ACCESS_KEY, DUMMY_AWS_SECRET_ACCESS_KEY),
                        Regions.EU_WEST_2);

        Assertions.assertThrows(AwsResponseException.class, () -> amazonEventSender.sendAuthenticated(anEventMessage().build(), ""));
    }

}

