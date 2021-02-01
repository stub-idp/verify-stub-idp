package uk.gov.ida.integrationTest;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
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
import static uk.gov.ida.integrationTest.support.HubResponseFactory.getUserAccountCreationResponse;

public class UserAccountCreationAppRuleTest extends IntegrationTestHelper {
    private static final String LOGIN_PATH = Urls.TestRpUrls.LOGIN_RESOURCE;
    private static Client client;
    private static JourneyHelper journeyHelper;

    @ClassRule
    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "https://localhost:"+getMsaStubRule().getSecurePort()+"/metadata"),
            ConfigOverride.config("hubExpectedToSignAuthnResponse", "true"));

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(UserAccountCreationAppRuleTest.class.getSimpleName());
        journeyHelper = new JourneyHelper(client);
    }

    @Test
    public void shouldDisplayAttributesForNewUser() throws Exception {
        final URI uri = testRp.uri(LOGIN_PATH);

        final RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(testRp.uri(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE));
        // force matching to occur, so hashed pid is matched when user returns
        journeyHelper.doASuccessfulMatchInLocalMatchingService(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), requestParams.getRequestId().get());

        Form form = new Form();
        form.param(Urls.Params.SAML_RESPONSE_PARAM, getUserAccountCreationResponse());
        form.param(Urls.Params.RELAY_STATE_PARAM, requestParams.getRelayState().get());
        Response response = client
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .target(uri)
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        final String body = response.readEntity(String.class);
        assertThat(body).contains("User Account created at TestRp");
        assertThat(body).contains("Your user account has been created");
        assertThat(body).contains("With a level of assurance LEVEL_2");
    }

}
