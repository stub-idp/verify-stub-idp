package stubidp.test.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.stubidp.builders.StubIdpBuilder.aStubIdp;

@ExtendWith(DropwizardExtensionsSupport.class)
public class LevelOfAssuranceIntegrationTests extends IntegrationTestHelper {

    public static final String IDP_NAME = "loa-idp";
    public static final String DISPLAY_NAME = "Level Of Assurance Identity Service";

    private final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    public static final StubIdpAppExtension applicationRule = new StubIdpAppExtension()
            .withStubIdp(aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build());

    private final AuthnRequestSteps authnRequestSteps = new AuthnRequestSteps(
            client,
            IDP_NAME,
            applicationRule.getLocalPort());

    @BeforeEach
    public void refreshMetadata() {
        client.target("http://localhost:"+applicationRule.getAdminPort()+"/tasks/metadata-refresh").request().post(Entity.text(""));
    }

    @Test
    public void debugPageShowsAuthnContextsAndComparisonType() throws Exception {
        final AuthnRequestSteps.Cookies cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp("hint");

        Response response = aUserVisitsTheDebugPage(IDP_NAME, cookies);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        Document doc = Jsoup.parse(response.readEntity(String.class));
        assertThat(doc.getElementById("authn-request-comparision-type").text()).isEqualTo("AuthnRequest comparison type is \"minimum\".");
        assertThat(getListItems(doc, "authn-contexts")).containsExactly("LEVEL_1", "LEVEL_2");
    }

    private List<String> getListItems(Document doc, String parentClass) {
        return doc.getElementsByClass(parentClass).stream()
                .flatMap(ul -> ul.getElementsByTag("li").stream())
                .map(Element::text).collect(Collectors.toList());
    }

    public Response aUserVisitsTheDebugPage(String idp, AuthnRequestSteps.Cookies cookies) {
        return client.target(getDebugPath(idp))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .get();
    }

    private String getDebugPath(String idp) {
        UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:"+applicationRule.getLocalPort()+Urls.IDP_DEBUG_RESOURCE);
        return uriBuilder.build(idp).toASCIIString();
    }

}
