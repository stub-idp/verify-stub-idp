package stubidp.test.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.integration.support.StubIdpBuilder.aStubIdp;

@ExtendWith(DropwizardExtensionsSupport.class)
public class AutoEscapingIntegrationTest extends IntegrationTestHelper {

    private static final String IDP_NAME = "auto-escaping-idp";
    private static final String DISPLAY_NAME = "Auto-Escaping Identity Service";

    private final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);
    private final AuthnRequestSteps authnRequestSteps = new AuthnRequestSteps(
            client,
            IDP_NAME,
            applicationRule.getLocalPort());

    public static final StubIdpAppExtension applicationRule = new StubIdpAppExtension(Map.ofEntries(Map.entry("secureCookieConfiguration.secure", "false")))
            .withStubIdp(aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build());

    @BeforeEach
    void before() {
        client.target("http://localhost:" + applicationRule.getAdminPort() + "/tasks/metadata-refresh").request().post(Entity.text(""));
    }

    @Test
    void userHasAnXSSHintAndItIsCorrectlyEscapedTest() {
        final String xss = "afd5j\"><script>alert(\"pwnage\")</script>c3tw";
        final AuthnRequestSteps.Cookies cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp(xss);
        final String response = userSeesTheHintOnTheDebugPage(cookies);
        assertThat(response).doesNotContain(xss);
        assertThat(response).contains("pwnage");
    }

    private String userSeesTheHintOnTheDebugPage(AuthnRequestSteps.Cookies cookies) {
        Response response = client.target(UriBuilder.fromUri("http://localhost:" + applicationRule.getLocalPort()).path(Urls.IDP_DEBUG_RESOURCE).build(IDP_NAME))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        return response.readEntity(String.class);
    }

}
