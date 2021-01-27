package stubidp.kotlin.test.integration.steps

import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter
import stubidp.stubidp.Urls
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLDecoder
import java.util.Arrays
import java.util.Objects
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder

class PreRegistrationSteps(private val client: Client, private val applicationRule: StubIdpAppExtension) {
    private var response: Response? = null
    val cookies: Cookies = Cookies()
    var csrfToken: String? = null
        private set
    private var responseEntity: String? = null

    fun userSuccessfullyNavigatesTo(path: String): PreRegistrationSteps {
        response = client.target(getUri(path))
                .request()
                .cookie(cookies.sessionCookie)
                .cookie(cookies.secureCookie)
                .get()
        Assertions.assertThat(response?.status).isEqualTo(Response.Status.OK.statusCode)
        responseEntity = response?.readEntity(String::class.java)
        val entity = Jsoup.parse(responseEntity)
        val csrfElement = entity.getElementById(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY)
        if (!Objects.isNull(csrfElement)) {
            csrfToken = entity.getElementById(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY).`val`()
        }
        cookies.extractCookies(response)
        return this
    }

    fun userIsRedirectedTo(path: String): PreRegistrationSteps {
        return userIsRedirectedTo(getUri(path))
    }

    private fun userIsRedirectedTo(uri: URI): PreRegistrationSteps {
        Assertions.assertThat(response!!.status).isEqualTo(Response.Status.SEE_OTHER.statusCode)
        var uriString: String? = null
        try {
            uriString = URLDecoder.decode(uri.toString(), "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            Assertions.fail<Any>("URI couldn't be decoded")
        }
        Assertions.assertThat(response!!.location.toString()).isEqualTo(uriString)
        return this
    }

    fun theRedirectIsFollowed(): PreRegistrationSteps {
        response = client.target(response!!.location)
                .request()
                .cookie(cookies.sessionCookie)
                .cookie(cookies.secureCookie)
                .get()
        cookies.extractCookies(response)
        responseEntity = response?.readEntity(String::class.java)
        return this
    }

    fun theResponseStatusIs(status: Response.Status): PreRegistrationSteps {
        Assertions.assertThat(response!!.status).isEqualTo(status.statusCode)
        return this
    }

    fun userSubmitsForm(form: Form, path: String): PreRegistrationSteps {
        return postFormTo(form, path)
    }

    fun clientPostsFormData(form: Form, path: String): PreRegistrationSteps {
        return postFormTo(form, path)
    }

    private fun postFormTo(form: Form, path: String): PreRegistrationSteps {
        return postFormTo(form, getUri(path))
    }

    private fun postFormTo(form: Form, uri: URI): PreRegistrationSteps {
        response = client.target(uri)
                .request()
                .cookie(cookies.sessionCookie)
                .cookie(cookies.secureCookie)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE))
        cookies.extractCookies(response)
        return this
    }

    fun responseContains(vararg content: String): PreRegistrationSteps {
        Arrays.stream(content).forEach { string: String? -> Assertions.assertThat(responseEntity).contains(string) }
        return this
    }

    private fun getUri(path: String): URI {
        return UriBuilder.fromUri("http://localhost:" + applicationRule.localPort)
                .path(path)
                .buildFromMap(java.util.Map.of(Urls.IDP_ID_PARAM, IDP_NAME))
    }

    companion object {
        private const val IDP_NAME = "stub-idp-demo-one"
        private const val DISPLAY_NAME = "Stub Idp One Pre-Register"
    }

}