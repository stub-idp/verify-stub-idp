package stubidp.kotlin.test.integration

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.assertj.core.api.Assertions
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import stubidp.kotlin.test.integration.steps.AuthnRequestSteps
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubidp.kotlin.test.integration.support.StubIdpBuilder
import stubidp.stubidp.Urls
import stubidp.stubidp.cookies.StubIdpCookieNames
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.UriBuilder

@ExtendWith(DropwizardExtensionsSupport::class)
class AutoEscapingIntegrationTest : IntegrationTestHelper() {
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
    fun userHasAnXSSHintAndItIsCorrectlyEscapedTest() {
        val xss = "afd5j\"><script>alert(\"pwnage\")</script>c3tw"
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp(xss)
        val response = userSeesTheHintOnTheDebugPage(cookies)
        Assertions.assertThat(response).doesNotContain(xss)
        Assertions.assertThat(response).contains("pwnage")
    }

    private fun userSeesTheHintOnTheDebugPage(cookies: AuthnRequestSteps.Cookies): String {
        val response = client.target(UriBuilder.fromUri("http://localhost:" + applicationRule.localPort).path(Urls.IDP_DEBUG_RESOURCE).build(IDP_NAME))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .get()
        Assertions.assertThat(response.status).isEqualTo(200)
        return response.readEntity(String::class.java)
    }

    companion object {
        private const val IDP_NAME = "auto-escaping-idp"
        private const val DISPLAY_NAME = "Auto-Escaping Identity Service"
        val applicationRule = StubIdpAppExtension(java.util.Map.ofEntries<String, String>(java.util.Map.entry<String, String>("secureCookieConfiguration.secure", "false")))
                .withStubIdp(StubIdpBuilder.aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build())
    }
}