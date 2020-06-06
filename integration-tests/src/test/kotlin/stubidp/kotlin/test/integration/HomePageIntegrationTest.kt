package stubidp.kotlin.test.integration

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter
import stubidp.stubidp.Urls
import stubidp.stubidp.builders.StubIdpBuilder
import stubidp.stubidp.domain.SubmitButtonValue
import stubidp.kotlin.test.integration.steps.FormBuilder
import stubidp.kotlin.test.integration.steps.PreRegistrationSteps
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Response

@ExtendWith(DropwizardExtensionsSupport::class)
class HomePageIntegrationTest : IntegrationTestHelper() {
    @BeforeEach
    fun setUp() {
        client.target("http://localhost:" + applicationRule.adminPort + "/tasks/metadata-refresh").request().post(Entity.text(""))
    }

    @Test
    fun shouldShowLinkToLogInWhenNotLoggedInTest() {
        val loggedOutUserVisitsHomePage = PreRegistrationSteps(client, applicationRule)
        loggedOutUserVisitsHomePage.userSuccessfullyNavigatesTo(Urls.SINGLE_IDP_HOMEPAGE_RESOURCE)
                .responseContains("Log In")
    }

    @Test
    fun shouldWelcomeUserWhenLoggedInTest() {
        val steps = PreRegistrationSteps(client, applicationRule)
        steps.userSuccessfullyNavigatesTo(Urls.IDP_LOGIN_RESOURCE)
                .clientPostsFormData(FormBuilder.newForm()
                        .withParam(Urls.IDP_ID_PARAM, IDP_NAME)
                        .withParam(Urls.USERNAME_PARAM, IDP_NAME)
                        .withParam(Urls.PASSWORD_PARAM, "bar")
                        .withParam(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY, steps.csrfToken)
                        .withParam(Urls.SUBMIT_PARAM, SubmitButtonValue.SignIn.toString())
                        .build(),
                        Urls.IDP_LOGIN_RESOURCE)
                .userIsRedirectedTo(Urls.SINGLE_IDP_HOMEPAGE_RESOURCE)
                .theRedirectIsFollowed()
                .theResponseStatusIs(Response.Status.OK)
                .responseContains("Welcome Jack Bauer", "Logout")
    }

    companion object {
        private const val IDP_NAME = "stub-idp-demo-one"
        private const val DISPLAY_NAME = "Stub Idp One Pre-Register"
        val applicationRule = StubIdpAppExtension(java.util.Map.of<String, String>("singleIdpJourney.enabled", "true"))
                .withStubIdp(StubIdpBuilder.aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build())
        var client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)
    }
}