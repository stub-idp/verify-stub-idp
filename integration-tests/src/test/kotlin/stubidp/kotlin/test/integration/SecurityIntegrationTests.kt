package stubidp.kotlin.test.integration

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.assertj.core.api.Assertions
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.jsoup.Jsoup
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import stubidp.saml.test.TestCredentialFactory
import stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter
import stubidp.stubidp.Urls
import stubidp.stubidp.builders.StubIdpBuilder
import stubidp.stubidp.cookies.StubIdpCookieNames
import stubidp.stubidp.filters.SecurityHeadersFilterTest
import stubidp.test.devpki.TestCertificateStrings
import stubidp.kotlin.test.integration.steps.AuthnRequestSteps
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubsp.stubsp.saml.request.IdpAuthnRequestBuilder
import java.util.Objects
import java.util.Optional
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form
import javax.ws.rs.core.UriBuilder

@ExtendWith(DropwizardExtensionsSupport::class)
class SecurityIntegrationTests : IntegrationTestHelper() {
    private val client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)
    private val authnRequestSteps = AuthnRequestSteps(
            client,
            IDP_NAME,
            applicationRule.localPort)

    @BeforeEach
    fun refreshMetadata() {
        client.target("http://localhost:" + applicationRule.adminPort + "/tasks/connector-metadata-refresh").request().post(Entity.text(""))
    }

    @Test
    fun securityHeaderTest() {
        val response = client.target(UriBuilder.fromUri("http://localhost:" + applicationRule.localPort)
                .path("/page_does_not_exist")
                .build())
                .request()
                .get()
        Assertions.assertThat(response.status).isEqualTo(404)
        SecurityHeadersFilterTest.checkSecurityHeaders(response.headers)
    }

    @Test
    fun csrfTokenIsUniquePerPageLoadTest() {
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp()
        val csrfValueOne = getLoginPageCsrfValue(cookies)
        val csrfValueTwo = getLoginPageCsrfValue(cookies)
        Assertions.assertThat(csrfValueOne).isNotEqualTo(csrfValueTwo)
    }

    @Test
    fun whenCsrfTokenIsModifiedThenRequestDoesNotWorkTest() {
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp()
        getLoginPageCsrfValue(cookies)
        val form = Form()
        form.param(Urls.USERNAME_PARAM, IDP_NAME)
        form.param(Urls.PASSWORD_PARAM, "bar")
        form.param(Urls.SUBMIT_PARAM, "SignIn")
        form.param(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY, "this_is_not_a_csrf_value")
        val response = client.target(authnRequestSteps.getStubIdpUri(Urls.IDP_LOGIN_RESOURCE))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .post(Entity.form(form))
        Assertions.assertThat(response.status).isEqualTo(500)
    }

    @Test
    fun whenSecureCookieIsModifiedThenRequestDoesNotWorkTest() {
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp()
        var response = client.target(authnRequestSteps.getStubIdpUri(Urls.IDP_LOGIN_RESOURCE))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .get()
        Assertions.assertThat(response.status).isEqualTo(200)
        response = client.target(authnRequestSteps.getStubIdpUri(Urls.IDP_LOGIN_RESOURCE))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, "try this")
                .get()
        Assertions.assertThat(response.status).isEqualTo(500)
    }

    @Test
    fun whenSameAuthnRequestIsSentTwiceItFailsTest() {
        val authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withDestination(UriBuilder.fromUri(authnRequestSteps.getStubIdpUri(Urls.IDP_SAML2_SSO_RESOURCE)).build().toASCIIString())
                .withSigningCredential(TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).signingCredential)
                .withSigningCertificate(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT)
                .withEntityId(StubIdpAppExtension.SP_ENTITY_ID)
                .build()
        var response = authnRequestSteps.postAuthnRequest(java.util.List.of<String>(), Optional.empty(), Optional.empty(), authnRequest, Optional.empty(), Urls.IDP_SAML2_SSO_RESOURCE)
        Assertions.assertThat(response.status).isEqualTo(303)
        response = authnRequestSteps.postAuthnRequest(java.util.List.of<String>(), Optional.empty(), Optional.empty(), authnRequest, Optional.empty(), Urls.IDP_SAML2_SSO_RESOURCE)
        Assertions.assertThat(response.status).isEqualTo(500)
    }

    private fun getLoginPageCsrfValue(cookies: AuthnRequestSteps.Cookies): String? {
        val response = client.target(authnRequestSteps.getStubIdpUri(Urls.IDP_LOGIN_RESOURCE))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .get()
        Assertions.assertThat(response.status).isEqualTo(200)
        val entity = Jsoup.parse(response.readEntity(String::class.java))
        val csrfElement = entity.getElementById(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY)
        return if (!Objects.isNull(csrfElement)) {
            csrfElement.`val`()
        } else null
    }

    companion object {
        private const val IDP_NAME = "stub-idp-one"
        private const val DISPLAY_NAME = "User Login Identity Service"
        val applicationRule = StubIdpAppExtension()
                .withStubIdp(StubIdpBuilder.aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build())
    }
}