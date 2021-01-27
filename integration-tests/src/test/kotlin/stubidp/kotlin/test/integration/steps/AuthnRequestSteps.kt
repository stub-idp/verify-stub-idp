package stubidp.kotlin.test.integration.steps

import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import org.opensaml.security.credential.Credential
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubidp.saml.extensions.IdaConstants
import stubidp.saml.security.IdaKeyStore
import stubidp.saml.test.TestCredentialFactory
import stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter
import stubidp.stubidp.Urls
import stubidp.stubidp.cookies.StubIdpCookieNames
import stubidp.stubidp.domain.FraudIndicator
import stubidp.stubidp.repositories.StubCountryRepository
import stubidp.stubidp.views.SignAssertions
import stubidp.test.devpki.TestCertificateStrings
import stubidp.test.devpki.TestEntityIds
import stubidp.utils.security.security.PrivateKeyFactory
import stubidp.utils.security.security.PublicKeyFactory
import stubidp.utils.security.security.X509CertificateFactory
import stubsp.stubsp.saml.request.EidasAuthnRequestBuilder
import stubsp.stubsp.saml.request.IdpAuthnRequestBuilder
import java.net.URI
import java.security.KeyPair
import java.util.Base64
import java.util.Objects
import java.util.Optional
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder

class AuthnRequestSteps(private val client: Client, private val idpName: String, private val port: Int) {

    class Cookies(val sessionId: String, val secure: String?)

    fun userPostsAuthnRequestToStubIdp(relayState: Optional<String>): Cookies {
        return userPostsAuthnRequestToStubIdp(listOf(), Optional.empty(), Optional.empty(), relayState)
    }

    fun userPostsAuthnRequestToStubIdp(hint: String?): Cookies {
        return userPostsAuthnRequestToStubIdp(listOf(hint), Optional.empty(), Optional.empty(), Optional.empty())
    }

    fun userPostsAuthnRequestToStubIdpReturnResponse(hints: List<String?>, language: Optional<String>, registration: Optional<Boolean>, withInvalidKey: Boolean, relayState: Optional<String>): Response {
        val signingCredential: Credential
        val signingCertificate: String
        if (withInvalidKey) {
            signingCredential = TestCredentialFactory(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT, TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY).signingCredential
            signingCertificate = TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT
        } else {
            signingCredential = TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).signingCredential
            signingCertificate = TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT
        }
        val authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withDestination(UriBuilder.fromUri("http://localhost:" + port + Urls.IDP_SAML2_SSO_RESOURCE).build(idpName).toASCIIString())
                .withSigningCredential(signingCredential)
                .withSigningCertificate(signingCertificate)
                .withEntityId(StubIdpAppExtension.SP_ENTITY_ID)
                .build()
        return postAuthnRequest(hints, language, registration, authnRequest, relayState, Urls.IDP_SAML2_SSO_RESOURCE)
    }

    fun userPostsAuthnRequestToHeadlessIdpReturnResponse(isCycle3: Boolean, relayState: String?): String {
        val headlessResource = "http://localhost:" + port + Urls.HEADLESS_ROOT
        val authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withDestination(headlessResource)
                .withSigningCredential(TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).signingCredential)
                .withSigningCertificate(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT)
                .withEntityId(StubIdpAppExtension.SP_ENTITY_ID)
                .build()
        val form = Form()
        form.param(Urls.CYCLE3_PARAM, java.lang.Boolean.toString(isCycle3))
        form.param(Urls.RELAY_STATE_PARAM, relayState)
        form.param(Urls.SAML_REQUEST_PARAM, authnRequest)
        val response = client.target(headlessResource)
                .request()
                .post(Entity.form(form))
        Assertions.assertThat(response.status).isEqualTo(200)
        return getSamlResponseFromResponseString(response.readEntity(String::class.java))
    }

    fun getSamlResponseFromResponseString(responseString: String?): String {
        val page = Jsoup.parse(responseString)
        Assertions.assertThat(page.getElementsByTag("title").text()).isEqualTo("Saml Processing...")
        return page.getElementsByAttributeValue("name", "SAMLResponse").`val`()
    }

    fun getRelayStateFromResponseHtml(entityString: String?): String {
        val page = Jsoup.parse(entityString)
        Assertions.assertThat(page.getElementsByTag("title").text()).isEqualTo("Saml Processing...")
        val relayStateElement = page.getElementById(Urls.RELAY_STATE_PARAM)
        return relayStateElement.`val`()
    }

    @JvmOverloads
    fun userPostsAuthnRequestToStubIdp(hints: List<String?> = listOf(), language: Optional<String> = Optional.empty(), registration: Optional<Boolean> = Optional.empty(), relayState: Optional<String> = Optional.empty()): Cookies {
        val response = userPostsAuthnRequestToStubIdpReturnResponse(hints, language, registration, false, relayState)
        Assertions.assertThat(response.status).isEqualTo(303)
        if (registration.isPresent && registration.get()) {
            Assertions.assertThat(response.location.path).startsWith(getStubIdpUri(Urls.IDP_REGISTER_RESOURCE).path)
        } else {
            Assertions.assertThat(response.location.path).startsWith(getStubIdpUri(Urls.IDP_LOGIN_RESOURCE).path)
        }
        return getCookiesAndFollowRedirect(response)
    }

    fun userPostsEidasAuthnRequestToStubIdp(): Cookies {
        return userPostsEidasAuthnRequestToStubIdpWithAttribute(
            requestAddress = false,
            requestGender = false,
            withKeyInfo = true,
            relayState = Optional.empty()
        )
    }

    fun userPostsEidasAuthnRequestToStubIdpWithAttribute(requestAddress: Boolean, requestGender: Boolean, relayState: Optional<String>): Cookies {
        return userPostsEidasAuthnRequestToStubIdpWithAttribute(requestAddress, requestGender, true, relayState)
    }

    private fun userPostsEidasAuthnRequestToStubIdpWithAttribute(requestAddress: Boolean, requestGender: Boolean, withKeyInfo: Boolean, relayState: Optional<String>): Cookies {
        val response = userPostsEidasAuthnRequestReturnResponse(requestAddress, requestGender, withKeyInfo, relayState)
        Assertions.assertThat(response.status).isEqualTo(303)
        Assertions.assertThat(response.location.path).startsWith(getStubIdpUri(Urls.EIDAS_LOGIN_RESOURCE).path)
        return getCookiesAndFollowRedirect(response)
    }

    fun userPostsEidasAuthnRequestReturnResponse(requestAddress: Boolean, requestGender: Boolean, withKeyInfo: Boolean, relayState: Optional<String>): Response {
        return userPostsEidasAuthnRequestReturnResponse(requestAddress, requestGender, withKeyInfo, false, relayState)
    }

    fun userPostsEidasAuthnRequestReturnResponse(requestAddress: Boolean, requestGender: Boolean, withKeyInfo: Boolean, withInvalidKey: Boolean, relayState: Optional<String>): Response {
        val eidasAuthnRequestBuilder = EidasAuthnRequestBuilder.anAuthnRequest()
        if (requestAddress) {
            eidasAuthnRequestBuilder.withRequestedAttribute(IdaConstants.Eidas_Attributes.CurrentAddress.NAME)
        }
        if (requestGender) {
            eidasAuthnRequestBuilder.withRequestedAttribute(IdaConstants.Eidas_Attributes.Gender.NAME)
        }
        eidasAuthnRequestBuilder.withIssuerEntityId(TestEntityIds.HUB_CONNECTOR_ENTITY_ID)
        eidasAuthnRequestBuilder.withKeyInfo(withKeyInfo)
        eidasAuthnRequestBuilder.withKeyStore(if (withInvalidKey) createInvalidIdaKeyStore() else createValidIdaKeyStore())
        eidasAuthnRequestBuilder.withDestination(UriBuilder.fromUri("http://localhost:" + port + Urls.EIDAS_SAML2_SSO_RESOURCE).build(idpName).toASCIIString())
        val authnRequest = eidasAuthnRequestBuilder.build()
        return postAuthnRequest(listOf(), Optional.empty(), Optional.empty(), authnRequest, relayState, Urls.EIDAS_SAML2_SSO_RESOURCE)
    }

    private fun getCookiesAndFollowRedirect(responseWithCookies: Response): Cookies {
        val sessionCookie = responseWithCookies.cookies[StubIdpCookieNames.SESSION_COOKIE_NAME]
        Assertions.assertThat(sessionCookie).isNotNull
        Assertions.assertThat(sessionCookie!!.value).isNotNull
        val sessionCookieValue = sessionCookie.value
        val secureCookie = responseWithCookies.cookies[StubIdpCookieNames.SECURE_COOKIE_NAME]
        val secureCookieValue = secureCookie?.value
        val response = client.target(responseWithCookies.location)
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, sessionCookieValue)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, secureCookieValue)
                .get()
        Assertions.assertThat(response.status).isEqualTo(200)
        return Cookies(sessionCookieValue, secureCookieValue)
    }

    fun postAuthnRequest(hints: List<String?>, language: Optional<String>, registration: Optional<Boolean>, authnRequest: String?, relayState: Optional<String>, ssoEndpoint: String?): Response {
        val form = Form()
        form.param(Urls.SAML_REQUEST_PARAM, authnRequest)
        registration.ifPresent { b: Boolean -> form.param(Urls.REGISTRATION_PARAM, b.toString()) }
        language.ifPresent { s: String? -> form.param(Urls.LANGUAGE_HINT_PARAM, s) }
        for (hint in hints) {
            form.param(Urls.HINTS_PARAM, hint)
        }
        form.param(Urls.RELAY_STATE_PARAM, relayState.orElse("relay_state"))
        return client.target(getStubIdpUri(ssoEndpoint))
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE))
    }

    fun userFailureFraud(cookies: Cookies): String {
        var response = client.target(getStubIdpUri(Urls.IDP_LOGIN_RESOURCE))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .get()
        val form = Form()
        form.param(Urls.LOGIN_FAILURE_STATUS_PARAM, FraudIndicator.DF01.name)
        val entity = Jsoup.parse(response.readEntity(String::class.java))
        val csrfElement = entity.getElementById(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY)
        if (!Objects.isNull(csrfElement)) {
            form.param(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY, entity.getElementById(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY).`val`())
        }
        response = client.target(getStubIdpUri(Urls.IDP_FRAUD_FAILURE_RESOURCE))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .post(Entity.form(form))
        Assertions.assertThat(response.status).isEqualTo(200)
        return getSamlResponseFromResponseString(response.readEntity(String::class.java))
    }

    fun eidasUserLogsIn(cookies: Cookies, signAssertions: Boolean) {
        userLogsIn(cookies, StubCountryRepository.STUB_COUNTRY_FRIENDLY_ID, Urls.EIDAS_LOGIN_RESOURCE, Urls.EIDAS_CONSENT_RESOURCE, signAssertions)
    }

    @JvmOverloads
    fun userLogsIn(cookies: Cookies, username: String = idpName) {
        userLogsIn(cookies, username, Urls.IDP_LOGIN_RESOURCE, Urls.IDP_CONSENT_RESOURCE, false)
    }

    private fun userLogsIn(cookies: Cookies, username: String, loginUrl: String, consentUrl: String, eidasSignAssertions: Boolean) {
        var response = client.target(getStubIdpUri(loginUrl))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .get()
        Assertions.assertThat(response.status).isEqualTo(200)
        val form = Form()
        form.param(Urls.USERNAME_PARAM, username)
        form.param(Urls.PASSWORD_PARAM, "bar")
        form.param(Urls.SUBMIT_PARAM, "SignIn")
        if (eidasSignAssertions) {
            form.param(Urls.SIGN_ASSERTIONS_PARAM, SignAssertions.signAssertions.name)
        }
        val entity = Jsoup.parse(response.readEntity(String::class.java))
        val csrfElement = entity.getElementById(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY)
        if (!Objects.isNull(csrfElement)) {
            form.param(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY, entity.getElementById(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY).`val`())
        }
        response = client.target(getStubIdpUri(loginUrl))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .post(Entity.form(form))
        Assertions.assertThat(response.status).isEqualTo(303)
        Assertions.assertThat(response.location.path).isEqualTo(getStubIdpUri(consentUrl).path)
    }

    fun userConsentsReturnResponse(cookies: Cookies, randomize: Boolean): Response {
        return userConsentsReturnResponse(cookies, randomize, Urls.IDP_CONSENT_RESOURCE, Optional.empty())
    }

    fun eidasUserConsentsReturnResponse(cookies: Cookies, randomize: Boolean, signingAlgorithm: String): Response {
        return userConsentsReturnResponse(cookies, randomize, Urls.EIDAS_CONSENT_RESOURCE, Optional.of(signingAlgorithm))
    }

    fun userConsentsReturnSamlResponse(cookies: Cookies, randomize: Boolean): String {
        return userConsentsReturnSamlResponse(cookies, randomize, Urls.IDP_CONSENT_RESOURCE, Optional.empty())
    }

    fun eidasUserConsentsReturnSamlResponse(cookies: Cookies, randomize: Boolean, signingAlgorithm: String): String {
        return userConsentsReturnSamlResponse(cookies, randomize, Urls.EIDAS_CONSENT_RESOURCE, Optional.of(signingAlgorithm))
    }

    private fun userConsentsReturnResponse(cookies: Cookies, randomize: Boolean, consentUrl: String, signingAlgorithm: Optional<String>): Response {
        var response = client.target(getStubIdpUri(consentUrl))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .get()
        Assertions.assertThat(response.status).isEqualTo(200)
        val form = Form()
        form.param(Urls.SUBMIT_PARAM, "I Agree")
        form.param(Urls.RANDOMISE_PID_PARAM, java.lang.Boolean.toString(randomize))
        val entity = Jsoup.parse(response.readEntity(String::class.java))
        val csrfElement = entity.getElementById(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY)
        if (!Objects.isNull(csrfElement)) {
            form.param(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY, entity.getElementById(AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY).`val`())
        }
        signingAlgorithm.ifPresent { s: String? -> form.param(Urls.SIGNING_ALGORITHM_PARAM, s) } // only for eidas consent POST
        response = client.target(getStubIdpUri(consentUrl))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE))
        Assertions.assertThat(response.status).isEqualTo(200)
        return response
    }

    private fun userConsentsReturnSamlResponse(cookies: Cookies, randomize: Boolean, consentUrl: String, signingAlgorithm: Optional<String>): String {
        val response = userConsentsReturnResponse(cookies, randomize, consentUrl, signingAlgorithm)
        Assertions.assertThat(response.status).isEqualTo(200)
        return getSamlResponseFromResponseString(response.readEntity(String::class.java))
    }

    fun userViewsTheDebugPage(cookies: Cookies): String {
        return userViewsTheDebugPage(cookies, getStubIdpUri(Urls.IDP_DEBUG_RESOURCE))
    }

    fun eidasUserViewsTheDebugPage(cookies: Cookies): String {
        val page = userViewsTheDebugPage(cookies, getStubIdpUri(Urls.EIDAS_DEBUG_RESOURCE))
        Assertions.assertThat(page).contains("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier")
        return page
    }

    private fun userViewsTheDebugPage(cookies: Cookies, debugUrl: URI): String {
        val response = client.target(debugUrl)
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .get()
        Assertions.assertThat(response.status).isEqualTo(200)
        // we should probably test more things
        val page = response.readEntity(String::class.java)
        Assertions.assertThat(page).contains(cookies.sessionId)
        return page
    }

    private fun createValidIdaKeyStore(): IdaKeyStore {
        val publicKeyFactory = PublicKeyFactory(X509CertificateFactory())
        val privateSigningKey = PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_SIGNING_KEY))
        val publicSigningKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT)
        val privateEncKey = PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_ENCRYPTION_KEY))
        val publicEncKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_ENCRYPTION_CERT)
        val signingKeyPair = KeyPair(publicSigningKey, privateSigningKey)
        val encryptionKeyPair = KeyPair(publicEncKey, privateEncKey)
        val certificate = X509CertificateFactory().createCertificate(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT)
        return IdaKeyStore(certificate, signingKeyPair, listOf(encryptionKeyPair))
    }

    private fun createInvalidIdaKeyStore(): IdaKeyStore {
        val publicKeyFactory = PublicKeyFactory(X509CertificateFactory())
        val privateSigningKey = PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY))
        val publicSigningKey = publicKeyFactory.createPublicKey(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT)
        val privateEncKey = PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY))
        val publicEncKey = publicKeyFactory.createPublicKey(TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT)
        val signingKeyPair = KeyPair(publicSigningKey, privateSigningKey)
        val encryptionKeyPair = KeyPair(publicEncKey, privateEncKey)
        val certificate = X509CertificateFactory().createCertificate(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT)
        return IdaKeyStore(certificate, signingKeyPair, listOf(encryptionKeyPair))
    }

    fun getStubIdpUri(path: String?): URI {
        return UriBuilder.fromUri("http://localhost:$port")
                .path(path)
                .build(idpName)
    }

}