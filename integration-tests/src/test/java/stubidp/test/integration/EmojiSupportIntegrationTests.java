package stubidp.test.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.test.integration.steps.AuthnRequestSteps;
import stubidp.test.integration.support.IntegrationTestHelper;
import stubidp.test.integration.support.StubIdpAppExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.integration.support.StubIdpBuilder.aStubIdp;

@ExtendWith(DropwizardExtensionsSupport.class)
public class EmojiSupportIntegrationTests extends IntegrationTestHelper {

    private static final String IDP_NAME = "stub-idp-one";
    private static final String DISPLAY_NAME = "Emoji Identity Service";

    private final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);
    private final AuthnRequestSteps authnRequestSteps = new AuthnRequestSteps(
            client,
            IDP_NAME,
            applicationRule.getLocalPort());

    private static final StubIdpAppExtension applicationRule = new StubIdpAppExtension()
            .withStubIdp(aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build());

    @BeforeEach
    void before() {
        client.target("http://localhost:"+applicationRule.getAdminPort()+"/tasks/metadata-refresh").request().post(Entity.text(""));
    }

    @Test
    void loginBehaviourTest() {
        final AuthnRequestSteps.Cookies cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp();
        authnRequestSteps.userLogsIn(cookies, IDP_NAME+"-emoji");
        final Document page = userConsents(cookies);
        assertThat(page.getElementById("firstName").text()).isEqualTo("😀");
        // can't do a direct comparison of the complete displayed text using jsoup
        assertThat(page.getElementById("address").text()).contains("🏠");
        assertThat(page.getElementById("address").text()).contains("🏘");
    }

    private Document userConsents(AuthnRequestSteps.Cookies cookies) {
        Response response = aStubIdpRequest(Urls.IDP_CONSENT_RESOURCE, cookies).get();

        assertThat(response.getStatus()).isEqualTo(200);

        final Document page = Jsoup.parse(response.readEntity(String.class));
        assertThat(page.getElementsByTag("title").text()).isEqualTo(format("Consent page for {0}", DISPLAY_NAME));

        return page;
    }

    private Invocation.Builder aStubIdpRequest(String path, AuthnRequestSteps.Cookies cookies) {
        return client.target(authnRequestSteps.getStubIdpUri(path))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure());
    }

}
