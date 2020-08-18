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
import java.util.stream.Collectors
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder
import kotlin.jvm.Throws

@ExtendWith(DropwizardExtensionsSupport::class)
class LevelOfAssuranceIntegrationTests : IntegrationTestHelper() {
    private val client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)
    private val authnRequestSteps = AuthnRequestSteps(
            client,
            IDP_NAME,
            applicationRule.localPort)

    @BeforeEach
    fun refreshMetadata() {
        client.target("http://localhost:" + applicationRule.adminPort + "/tasks/metadata-refresh").request().post(Entity.text(""))
    }

    @Test
    @Throws(Exception::class)
    fun debugPageShowsAuthnContextsAndComparisonTypeTest() {
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp("hint")
        val response = aUserVisitsTheDebugPage(IDP_NAME, cookies)
        Assertions.assertThat(response.status).isEqualTo(Response.Status.OK.statusCode)
        val doc = Jsoup.parse(response.readEntity(String::class.java))
        Assertions.assertThat(doc.getElementById("authn-request-comparision-type").text()).isEqualTo("AuthnRequest comparison type is \"minimum\".")
        Assertions.assertThat(getListItems(doc, "authn-contexts")).containsExactly("LEVEL_1", "LEVEL_2")
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
        val uriBuilder = UriBuilder.fromPath("http://localhost:" + applicationRule.localPort + Urls.IDP_DEBUG_RESOURCE)
        return uriBuilder.build(idp).toASCIIString()
    }

    companion object {
        const val IDP_NAME = "loa-idp"
        const val DISPLAY_NAME = "Level Of Assurance Identity Service"
        val applicationRule = StubIdpAppExtension()
                .withStubIdp(StubIdpBuilder.aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build())
    }
}