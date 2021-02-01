package uk.gov.ida.integrationTest;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.util.Duration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.integrationTest.support.IntegrationTestHelper;
import uk.gov.ida.integrationTest.support.JourneyHelper;
import uk.gov.ida.integrationTest.support.RequestParamHelper;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.Urls;
import uk.gov.ida.rp.testrp.domain.JourneyHint;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.rp.testrp.Urls.Cookies.TEST_RP_SESSION_COOKIE_NAME;
import static uk.gov.ida.rp.testrp.Urls.Params.JOURNEY_HINT_PARAM;

public class FullJourneyAppRuleTests extends IntegrationTestHelper {
    private static Client client;
    private static JourneyHelper journeyHelper;

    @ClassRule
    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "http://localhost:"+getMsaStubRule().getPort()+"/metadata"),
            ConfigOverride.config("allowInsecureMetadataLocation", "true"));

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(FullJourneyAppRuleTests.class.getSimpleName());
        journeyHelper = new JourneyHelper(client);
    }

    @Test
    public void ensureSuccessPageDisplaysCorrectlyAtEndOfJourney() throws MarshallingException, SignatureException {

        final RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(testRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE));
        final String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());
        final Response responseFromHub = journeyHelper.postSuccessAuthnResponseBackFromHub(testRp.uri(Urls.TestRpUrls.LOGIN_RESOURCE), hashedPid, requestParams.getRelayState().get());

        final Response testRpSuccessPage = journeyHelper.getSuccessPage(responseFromHub.getHeaderString("Location"), responseFromHub.getCookies().get(TEST_RP_SESSION_COOKIE_NAME));
        assertThat(testRpSuccessPage.readEntity(String.class)).contains("LEVEL_2");
    }

    @Test
    public void ensureNonRepuditationStepWorks() throws MarshallingException, SignatureException {

        RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(testRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE));
        String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());
        Response responseFromHub = journeyHelper.postSuccessAuthnResponseBackFromHub(testRp.uri(Urls.TestRpUrls.LOGIN_RESOURCE), hashedPid, requestParams.getRelayState().get());

        Response testRpSuccessPage = journeyHelper.getSuccessPage(responseFromHub.getHeaderString("Location"), responseFromHub.getCookies().get(TEST_RP_SESSION_COOKIE_NAME));
        assertThat(testRpSuccessPage.readEntity(String.class)).contains("LEVEL_2");

        URI uri = testRp.uriBuilder(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE)
                .queryParam(JOURNEY_HINT_PARAM, JourneyHint.submission_confirmation)
                .build();
        requestParams = journeyHelper.startNewJourneyFromTestRp(uri, false, false, requestParams.getRelayState());
        hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());
        responseFromHub = journeyHelper.postSuccessAuthnResponseBackFromHub(testRp.uri(Urls.TestRpUrls.LOGIN_RESOURCE), hashedPid, requestParams.getRelayState().get());

        testRpSuccessPage = journeyHelper.getSuccessPage(responseFromHub.getHeaderString("Location"), responseFromHub.getCookies().get(TEST_RP_SESSION_COOKIE_NAME));
        assertThat(testRpSuccessPage.readEntity(String.class)).contains("LEVEL_2");
    }

    @Test
    public void ensureRegistrationHintWorks() throws MarshallingException, SignatureException {

        URI uri = testRp.uriBuilder(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE)
                .queryParam(JOURNEY_HINT_PARAM, JourneyHint.registration)
                .build();
        
        RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(uri);
        String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());
        Response responseFromHub = journeyHelper.postSuccessAuthnResponseBackFromHub(testRp.uri(Urls.TestRpUrls.LOGIN_RESOURCE), hashedPid, requestParams.getRelayState().get());

        Response testRpSuccessPage = journeyHelper.getSuccessPage(responseFromHub.getHeaderString("Location"), responseFromHub.getCookies().get(TEST_RP_SESSION_COOKIE_NAME));
        assertThat(testRpSuccessPage.readEntity(String.class)).contains("LEVEL_2");
    }

    @Test
    public void ensureSignInHintWorks() throws MarshallingException, SignatureException {

        URI uri = testRp.uriBuilder(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE)
                .queryParam(JOURNEY_HINT_PARAM, JourneyHint.uk_idp_sign_in)
                .build();

        RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(uri);
        String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());
        Response responseFromHub = journeyHelper.postSuccessAuthnResponseBackFromHub(testRp.uri(Urls.TestRpUrls.LOGIN_RESOURCE), hashedPid, requestParams.getRelayState().get());

        Response testRpSuccessPage = journeyHelper.getSuccessPage(responseFromHub.getHeaderString("Location"), responseFromHub.getCookies().get(TEST_RP_SESSION_COOKIE_NAME));
        assertThat(testRpSuccessPage.readEntity(String.class)).contains("LEVEL_2");
    }

    @Test
    public void ensureNewJourneysWorkFromStartPageWhenLoggedIn() throws MarshallingException, SignatureException {

        final RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(testRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE));
        final String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());
        final Response responseFromHub = journeyHelper.postSuccessAuthnResponseBackFromHub(testRp.uri(Urls.TestRpUrls.LOGIN_RESOURCE), hashedPid, requestParams.getRelayState().get());
        final Response testRpSuccessPage = journeyHelper.getSuccessPage(responseFromHub.getHeaderString("Location"), responseFromHub.getCookies().get(TEST_RP_SESSION_COOKIE_NAME));
        assertThat(testRpSuccessPage.readEntity(String.class)).contains("LEVEL_2");

        final Response requestResponse = client
                .target(testRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE))
                .request()
                // we are logged in
                .cookie(responseFromHub.getCookies().get(TEST_RP_SESSION_COOKIE_NAME))
                .get();
        assertThat(requestResponse.getStatus()).isEqualTo(200);

    }

}
