package stubidp.kotlin.test.integration

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.assertj.core.api.Assertions
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import stubidp.kotlin.test.integration.steps.AuthnRequestSteps
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubidp.kotlin.test.integration.support.StubIdpBuilder
import stubidp.stubidp.Urls
import stubidp.stubidp.cookies.StubIdpCookieNames
import java.text.MessageFormat
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation

@ExtendWith(DropwizardExtensionsSupport::class)
class EmojiSupportIntegrationTests : IntegrationTestHelper() {
    private val client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)
    private val authnRequestSteps = AuthnRequestSteps(
            client,
            IDP_NAME,
            applicationRule.localPort)

    @BeforeEach
    fun before() {
        client.target("http://localhost:" + applicationRule.adminPort + "/tasks/metadata-refresh").request().post(Entity.text(""))
    }

    @Test
    fun loginBehaviourTest() {
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp()
        authnRequestSteps.userLogsIn(cookies, "$IDP_NAME-emoji")
        val page = userConsents(cookies)
        Assertions.assertThat(page.getElementById("firstName").text()).isEqualTo("😀")
        // can't do a direct comparison of the complete displayed text using jsoup
        Assertions.assertThat(page.getElementById("address").text()).contains("🏠")
        Assertions.assertThat(page.getElementById("address").text()).contains("🏘")
    }

    private fun userConsents(cookies: AuthnRequestSteps.Cookies): Document {
        val response = aStubIdpRequest(Urls.IDP_CONSENT_RESOURCE, cookies).get()
        Assertions.assertThat(response.status).isEqualTo(200)
        val page = Jsoup.parse(response.readEntity(String::class.java))
        Assertions.assertThat(page.getElementsByTag("title").text()).isEqualTo(MessageFormat.format("Consent page for {0}", DISPLAY_NAME))
        return page
    }

    private fun aStubIdpRequest(path: String, cookies: AuthnRequestSteps.Cookies): Invocation.Builder {
        return client.target(authnRequestSteps.getStubIdpUri(path))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
    }

    companion object {
        private const val IDP_NAME = "stub-idp-one"
        private const val DISPLAY_NAME = "Emoji Identity Service"
        val applicationRule = StubIdpAppExtension()
                .withStubIdp(StubIdpBuilder.aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build())
    }
}