package stubidp.test.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.stubidp.Urls;
import stubidp.stubidp.domain.SubmitButtonValue;
import stubidp.test.integration.steps.FormBuilder;
import stubidp.test.integration.steps.PreRegistrationSteps;
import stubidp.test.integration.support.IntegrationTestHelper;
import stubidp.test.integration.support.StubIdpAppExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Map;

import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY;
import static stubidp.stubidp.builders.StubIdpBuilder.aStubIdp;

@ExtendWith(DropwizardExtensionsSupport.class)
public class HomePageIntegrationTest extends IntegrationTestHelper {

    private static final String IDP_NAME = "stub-idp-demo-one";
    private static final String DISPLAY_NAME = "Stub Idp One Pre-Register";

    public static final StubIdpAppExtension applicationRule = new StubIdpAppExtension(Map.of("singleIdpJourney.enabled", "true"))
            .withStubIdp(aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build());

    public static Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    @BeforeEach
    void setUp() {
        client.target("http://localhost:" + applicationRule.getAdminPort() + "/tasks/metadata-refresh").request().post(Entity.text(""));
    }

    @Test
    void shouldShowLinkToLogInWhenNotLoggedIn() {
        PreRegistrationSteps loggedOutUserVisitsHomePage = new PreRegistrationSteps(client, applicationRule);

        loggedOutUserVisitsHomePage.userSuccessfullyNavigatesTo(Urls.SINGLE_IDP_HOMEPAGE_RESOURCE)
                .responseContains("Log In");

    }

    @Test
    void shouldWelcomeUserWhenLoggedIn() {
       PreRegistrationSteps steps = new PreRegistrationSteps(client, applicationRule);

        steps.userSuccessfullyNavigatesTo(Urls.IDP_LOGIN_RESOURCE)
                .clientPostsFormData(FormBuilder.newForm()
                        .withParam(Urls.IDP_ID_PARAM, IDP_NAME)
                        .withParam(Urls.USERNAME_PARAM, IDP_NAME)
                        .withParam(Urls.PASSWORD_PARAM,"bar")
                        .withParam(CSRF_PROTECT_FORM_KEY, steps.getCsrfToken())
                        .withParam(Urls.SUBMIT_PARAM, SubmitButtonValue.SignIn.toString())
                        .build(),
                        Urls.IDP_LOGIN_RESOURCE)
                .userIsRedirectedTo(Urls.SINGLE_IDP_HOMEPAGE_RESOURCE)
                .theRedirectIsFollowed()
                .theResponseStatusIs(Response.Status.OK)
                .responseContains("Welcome Jack Bauer", "Logout");
    }
}
