package stubidp.kotlin.test.integration

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.assertj.core.api.Assertions
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import stubidp.kotlin.test.integration.steps.AuthnRequestSteps
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubidp.kotlin.test.integration.support.StubIdpBuilder
import stubidp.stubidp.Urls
import stubidp.stubidp.cookies.StubIdpCookieNames
import stubidp.stubidp.domain.IdpHint
import java.util.Optional
import java.util.stream.Collectors
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder

@ExtendWith(DropwizardExtensionsSupport::class)
class HintsIntegrationTests : IntegrationTestHelper() {
    private var authnRequestSteps: AuthnRequestSteps? = null
    var client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)

    @BeforeEach
    fun setUp() {
        client.target("http://localhost:" + applicationRule.adminPort + "/tasks/metadata-refresh").request().post(Entity.text(""))
        authnRequestSteps = AuthnRequestSteps(
                client,
                IDP_NAME,
                applicationRule.localPort)
    }

    @Test
    fun debugPageShowsHintsTest() {
        val hints = java.util.List.of(IdpHint.has_apps.name, "snakes", "plane")
        val registration = Optional.of(true)
        val language = Optional.empty<String>()
        val cookies = authnRequestSteps!!.userPostsAuthnRequestToStubIdp(hints, language, registration, Optional.empty())
        val response = aUserVisitsTheDebugPage(IDP_NAME, cookies)
        Assertions.assertThat(response.status).isEqualTo(Response.Status.OK.statusCode)
        val doc = Jsoup.parse(response.readEntity(String::class.java))
        Assertions.assertThat(getListItems(doc, "known-hints")).containsExactly(IdpHint.has_apps.name)
        Assertions.assertThat(getListItems(doc, "unknown-hints")).containsExactlyInAnyOrder("snakes", "plane")
        Assertions.assertThat(doc.getElementById("language-hint").text()).isEqualTo("No language hint was set.")
        Assertions.assertThat(doc.getElementById("registration").text()).isEqualTo("\"registration\" hint is \"true\"")
    }

    @Test
    fun debugPageShowsLanguageHintTest() {
        val hints = java.util.List.of<String>()
        val registration = Optional.empty<Boolean>()
        val language = Optional.of("cy")
        val cookies = authnRequestSteps!!.userPostsAuthnRequestToStubIdp(hints, language, registration, Optional.empty())
        val response = aUserVisitsTheDebugPage(IDP_NAME, cookies)
        Assertions.assertThat(response.status).isEqualTo(Response.Status.OK.statusCode)
        val doc = Jsoup.parse(response.readEntity(String::class.java))
        val languageHintElement = doc.getElementById("language-hint")
        Assertions.assertThat(languageHintElement).isNotNull
        Assertions.assertThat(languageHintElement.text()).contains("\"cy\"")
        Assertions.assertThat(doc.getElementById("registration").text()).isEqualTo("\"registration\" hint not received")
    }

    private fun getListItems(doc: Document, parentClass: String): List<String> {
        return doc.getElementsByClass(parentClass).stream()
                .flatMap { ul: Element -> ul.getElementsByTag("li").stream() }
                .map { obj: Element -> obj.text() }.collect(Collectors.toList())
    }

    fun aUserVisitsTheDebugPage(idp: String, cookies: AuthnRequestSteps.Cookies): Response {
        return client.target(getDebugPath(idp))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .get()
    }

    private fun getDebugPath(idp: String): String {
        val uriBuilder = UriBuilder.fromPath("http://localhost:" + applicationRule.localPort + Urls.IDP_DEBUG_RESOURCE.replace("{idpId}", IDP_NAME))
        return uriBuilder.build(idp).toASCIIString()
    }

    companion object {
        private const val IDP_NAME = "stub-idp-one"
        private const val DISPLAY_NAME = "Hints Identity Service"
        val applicationRule = StubIdpAppExtension()
                .withStubIdp(StubIdpBuilder.aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build())
    }
}