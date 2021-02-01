package uk.gov.ida.integrationTest;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.integrationTest.support.IntegrationTestHelper;
import uk.gov.ida.integrationTest.support.JourneyHelper;
import uk.gov.ida.integrationTest.support.RequestParamHelper;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.Urls;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.integrationTest.support.HubResponseFactory.getSignedNoMatchResponse;
import static uk.gov.ida.integrationTest.support.HubResponseFactory.getSignedResponse;
import static uk.gov.ida.integrationTest.support.HubResponseFactory.getUnsignedNoMatchResponse;
import static uk.gov.ida.integrationTest.support.HubResponseFactory.getUnsignedResponse;
import static uk.gov.ida.rp.testrp.Urls.Cookies.TEST_RP_SESSION_COOKIE_NAME;

public class TestRpLoginResourceAppRuleTest extends IntegrationTestHelper {
    private static final String LOGIN_PATH = Urls.TestRpUrls.LOGIN_RESOURCE;
    private static Client client;
    private static JourneyHelper journeyHelper;

    @ClassRule
    public static TestRpAppRule wantsHubSignatureTestRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "https://localhost:"+getMsaStubRule().getSecurePort()+"/metadata"),
            ConfigOverride.config("hubExpectedToSignAuthnResponse", "true"));

    @ClassRule
    public static TestRpAppRule wantsNoHubSignatureTestRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "https://localhost:"+getMsaStubRule().getSecurePort()+"/metadata"),
            ConfigOverride.config("hubExpectedToSignAuthnResponse", "false"));

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().build();
        client = new JerseyClientBuilder(wantsHubSignatureTestRp.getEnvironment()).using(jerseyClientConfiguration).build(TestRpLoginResourceAppRuleTest.class.getSimpleName());
        journeyHelper = new JourneyHelper(client);
    }

    @Test
    public void shouldHandleNoMatchWithSuccessStatusForLegacyNonSimpleProfile() throws Exception {
        final URI uri = wantsHubSignatureTestRp.uri(LOGIN_PATH);

        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getSignedNoMatchResponse(StatusCode.SUCCESS));
        form.param(Urls.Params.RELAY_STATE_PARAM, "relayState");
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
    }

    @Test
    public void shouldHandleNoMatchWithResponderStatusForNonSimpleProfile() throws Exception {
        final URI uri = wantsHubSignatureTestRp.uri(LOGIN_PATH);

        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getSignedNoMatchResponse(StatusCode.RESPONDER));
        form.param(Urls.Params.RELAY_STATE_PARAM, "relayState");
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
    }

    @Test
    public void shouldHandleNoMatchWithResponderStatusForSimpleProfile() throws Exception {
        final URI uri = wantsNoHubSignatureTestRp.uri(LOGIN_PATH);

        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getUnsignedNoMatchResponse());
        form.param(Urls.Params.RELAY_STATE_PARAM, "relayState");
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
    }

    @Test
    public void shouldRedirectToSuccessPageWhenResponseEnvelopeIsExpectedlySigned() throws Exception {
        final URI uri = wantsHubSignatureTestRp.uri(LOGIN_PATH);

        final RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(wantsHubSignatureTestRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE));
        // force matching to occur, so hashed pid is matched when user returns
        final String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(wantsHubSignatureTestRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());

        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getSignedResponse(hashedPid));
        form.param(Urls.Params.RELAY_STATE_PARAM, requestParams.getRelayState().get());
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getCookies().containsKey(TEST_RP_SESSION_COOKIE_NAME)).isTrue();
    }

    @Test
    public void shouldNotRedirectToSuccessPageWhenResponseEnvelopeIsUnexpectedlyNotSigned() throws Exception {
        final URI uri = wantsHubSignatureTestRp.uri(LOGIN_PATH);

        final RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(wantsHubSignatureTestRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE));
        // force matching to occur, so hashed pid is matched when user returns
        final String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(wantsHubSignatureTestRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());

        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getUnsignedResponse(hashedPid));
        form.param(Urls.Params.RELAY_STATE_PARAM, requestParams.getRelayState().get());
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
    
    @Test
    public void shouldRedirectToSuccessPageWhenResponseEnvelopeIsUnexpectedlySigned() throws Exception {
        final URI uri = wantsNoHubSignatureTestRp.uri(LOGIN_PATH);

        final RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(wantsNoHubSignatureTestRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE));
        // force matching to occur, so hashed pid is matched when user returns
        final String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(wantsNoHubSignatureTestRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());

        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getSignedResponse(hashedPid));
        form.param(Urls.Params.RELAY_STATE_PARAM, requestParams.getRelayState().get());
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getCookies().containsKey(TEST_RP_SESSION_COOKIE_NAME)).isTrue();
    }

    @Test
    public void shouldRedirectToSuccessPageWhenResponseEnvelopeIsExpectedlyNotSigned() throws Exception {
        final URI uri = wantsNoHubSignatureTestRp.uri(LOGIN_PATH);

        final RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(wantsNoHubSignatureTestRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE));
        // force matching to occur, so hashed pid is matched when user returns
        final String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(wantsNoHubSignatureTestRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());

        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getUnsignedResponse(hashedPid));
        form.param(Urls.Params.RELAY_STATE_PARAM, requestParams.getRelayState().get());
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getCookies().containsKey(TEST_RP_SESSION_COOKIE_NAME)).isTrue();
    }

    @Test
    public void shouldRedirectToSuccessPageWhenResponseEnvelopeIsSigned() throws Exception {
        final URI uri = wantsHubSignatureTestRp.uri(LOGIN_PATH);

        final RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(wantsHubSignatureTestRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE));
        // force matching to occur, so hashed pid is matched when user returns
        final String hashedPid = journeyHelper.doASuccessfulMatchInLocalMatchingService(wantsHubSignatureTestRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());

        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getSignedResponse(hashedPid));
        form.param(Urls.Params.RELAY_STATE_PARAM, requestParams.getRelayState().get());
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getCookies().containsKey(TEST_RP_SESSION_COOKIE_NAME)).isTrue();
    }

}
