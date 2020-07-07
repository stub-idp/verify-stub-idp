package stubidp.test.integration.steps;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.opensaml.security.credential.Credential;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.domain.FraudIndicator;
import stubidp.stubidp.views.SignAssertions;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;
import stubsp.stubsp.saml.request.EidasAuthnRequestBuilder;
import stubsp.stubsp.saml.request.IdpAuthnRequestBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY;
import static stubidp.stubidp.Urls.RELAY_STATE_PARAM;
import static stubidp.stubidp.repositories.StubCountryRepository.STUB_COUNTRY_FRIENDLY_ID;
import static stubidp.test.devpki.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static stubidp.test.integration.support.StubIdpAppExtension.SP_ENTITY_ID;

public class AuthnRequestSteps {
    private final Client client;
    private final String idpName;
    private int port;

    public static class Cookies {
        private final String sessionId;
        private final String secure;

        public Cookies(String sessionId, String secure) {
            this.sessionId = sessionId;
            this.secure = secure;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getSecure() {
            return secure;
        }
    }

    public AuthnRequestSteps(Client client, String idpName, int port) {
        this.client = client;
        this.idpName = idpName;
        this.port = port;
    }

    public Cookies userPostsAuthnRequestToStubIdp(Optional<String> relayState) {
        return userPostsAuthnRequestToStubIdp(List.of(), Optional.empty(), Optional.empty(), relayState);
    }

    public Cookies userPostsAuthnRequestToStubIdp() {
        return userPostsAuthnRequestToStubIdp(List.of(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public Cookies userPostsAuthnRequestToStubIdp(String hint) {
        return userPostsAuthnRequestToStubIdp(List.of(hint), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public Response userPostsAuthnRequestToStubIdpReturnResponse(List<String> hints, Optional<String> language, Optional<Boolean> registration, boolean withInvalidKey, Optional<String> relayState) {
        Credential signingCredential;
        String signingCertificate;
        if(withInvalidKey) {
            signingCredential = new TestCredentialFactory(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT, TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY).getSigningCredential();
            signingCertificate = TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
        } else {
            signingCredential = new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential();
            signingCertificate = TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
        }
        String authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withDestination(UriBuilder.fromUri("http://localhost:"+port+Urls.IDP_SAML2_SSO_RESOURCE).build(idpName).toASCIIString())
                .withSigningCredential(signingCredential)
                .withSigningCertificate(signingCertificate)
                .withEntityId(SP_ENTITY_ID)
                .build();
        return postAuthnRequest(hints, language, registration, authnRequest, relayState, Urls.IDP_SAML2_SSO_RESOURCE);
    }

    public String userPostsAuthnRequestToHeadlessIdpReturnResponse(boolean isCycle3, String relayState) {
        final String headlessResource = "http://localhost:" + port + Urls.HEADLESS_ROOT;
        String authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withDestination(headlessResource)
                .withSigningCredential(new TestCredentialFactory(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY).getSigningCredential())
                .withSigningCertificate(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT)
                .withEntityId(SP_ENTITY_ID)
                .build();
        Form form = new Form();
        form.param(Urls.CYCLE3_PARAM, Boolean.toString(isCycle3));
        form.param(RELAY_STATE_PARAM, relayState);
        form.param(Urls.SAML_REQUEST_PARAM, authnRequest);

        Response response = client.target(headlessResource)
                .request()
                .post(Entity.form(form));
        assertThat(response.getStatus()).isEqualTo(200);
        return getSamlResponseFromResponseString(response.readEntity(String.class));
    }

    public String getSamlResponseFromResponseString(String responseString) {
        final Document page = Jsoup.parse(responseString);
        assertThat(page.getElementsByTag("title").text()).isEqualTo("Saml Processing...");
        return page.getElementsByAttributeValue("name", "SAMLResponse").val();
    }

    public String getRelayStateFromResponseHtml(String entityString) {
        final Document page = Jsoup.parse(entityString);
        assertThat(page.getElementsByTag("title").text()).isEqualTo("Saml Processing...");
        final Element relayStateElement = page.getElementById(RELAY_STATE_PARAM);
        return relayStateElement.val();
    }

    public Cookies userPostsAuthnRequestToStubIdp(List<String> hints, Optional<String> language, Optional<Boolean> registration, Optional<String> relayState) {
        Response response = userPostsAuthnRequestToStubIdpReturnResponse(hints, language, registration, false, relayState);

        assertThat(response.getStatus()).isEqualTo(303);
        if(registration.isPresent() && registration.get()) {
            assertThat(response.getLocation().getPath()).startsWith(getStubIdpUri(Urls.IDP_REGISTER_RESOURCE).getPath());
        } else {
            assertThat(response.getLocation().getPath()).startsWith(getStubIdpUri(Urls.IDP_LOGIN_RESOURCE).getPath());
        }

        return getCookiesAndFollowRedirect(response);
    }

    public Cookies userPostsEidasAuthnRequestToStubIdp() {
        return userPostsEidasAuthnRequestToStubIdpWithAttribute(false, false, true, Optional.empty());
    }

    public Cookies userPostsEidasAuthnRequestToStubIdpWithAttribute(boolean requestAddress, boolean requestGender) {
        return userPostsEidasAuthnRequestToStubIdpWithAttribute(requestAddress, requestGender, true, Optional.empty());
    }

    public Cookies userPostsEidasAuthnRequestToStubIdpWithAttribute(boolean requestAddress, boolean requestGender, Optional<String> relayState) {
        return userPostsEidasAuthnRequestToStubIdpWithAttribute(requestAddress, requestGender, true, relayState);
    }

    private Cookies userPostsEidasAuthnRequestToStubIdpWithAttribute(boolean requestAddress, boolean requestGender, boolean withKeyInfo, Optional<String> relayState) {
        Response response = userPostsEidasAuthnRequestReturnResponse(requestAddress, requestGender, withKeyInfo, relayState);

        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getLocation().getPath()).startsWith(getStubIdpUri(Urls.EIDAS_LOGIN_RESOURCE).getPath());

        return getCookiesAndFollowRedirect(response);
    }

    public Response userPostsEidasAuthnRequestReturnResponse(boolean requestAddress, boolean requestGender, boolean withKeyInfo, Optional<String> relayState) {
        return userPostsEidasAuthnRequestReturnResponse(requestAddress, requestGender, withKeyInfo, false, relayState);
    }

    public Response userPostsEidasAuthnRequestReturnResponse(boolean requestAddress, boolean requestGender, boolean withKeyInfo, boolean withInvalidKey, Optional<String> relayState) {
        final EidasAuthnRequestBuilder eidasAuthnRequestBuilder = EidasAuthnRequestBuilder.anAuthnRequest();
        if(requestAddress) {
            eidasAuthnRequestBuilder.withRequestedAttribute(IdaConstants.Eidas_Attributes.CurrentAddress.NAME);
        }
        if(requestGender) {
            eidasAuthnRequestBuilder.withRequestedAttribute(IdaConstants.Eidas_Attributes.Gender.NAME);
        }
        eidasAuthnRequestBuilder.withIssuerEntityId(HUB_CONNECTOR_ENTITY_ID);
        eidasAuthnRequestBuilder.withKeyInfo(withKeyInfo);
        eidasAuthnRequestBuilder.withKeyStore(withInvalidKey?createInvalidIdaKeyStore():createValidIdaKeyStore());
        eidasAuthnRequestBuilder.withDestination(UriBuilder.fromUri("http://localhost:"+port+Urls.EIDAS_SAML2_SSO_RESOURCE).build(idpName).toASCIIString());
        String authnRequest = eidasAuthnRequestBuilder.build();
        return postAuthnRequest(List.of(), Optional.empty(), Optional.empty(), authnRequest, relayState, Urls.EIDAS_SAML2_SSO_RESOURCE);
    }

    private Cookies getCookiesAndFollowRedirect(Response response) {
        final NewCookie sessionCookie = response.getCookies().get(StubIdpCookieNames.SESSION_COOKIE_NAME);
        assertThat(sessionCookie).isNotNull();
        assertThat(sessionCookie.getValue()).isNotNull();
        final String sessionCookieValue = sessionCookie.getValue();

        final NewCookie secureCookie = response.getCookies().get(StubIdpCookieNames.SECURE_COOKIE_NAME);
        final String secureCookieValue = secureCookie==null?null:secureCookie.getValue();

        response = client.target(response.getLocation())
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, sessionCookieValue)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, secureCookieValue)
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        return new Cookies(sessionCookieValue, secureCookieValue);
    }

    public Response postAuthnRequest(List<String> hints, Optional<String> language, Optional<Boolean> registration, String authnRequest, Optional<String> relayState, String ssoEndpoint) {
        Form form = new Form();
        form.param(Urls.SAML_REQUEST_PARAM, authnRequest);
        registration.ifPresent(b -> form.param(Urls.REGISTRATION_PARAM, b.toString()));
        language.ifPresent(s -> form.param(Urls.LANGUAGE_HINT_PARAM, s));
        for(String hint : hints) {
            form.param(Urls.HINTS_PARAM, hint);
        }
        form.param(RELAY_STATE_PARAM, relayState.orElse("relay_state"));

        return client.target(getStubIdpUri(ssoEndpoint))
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    }

    public void userLogsIn(Cookies cookies) {
        userLogsIn(cookies, idpName);
    }

    public String userFailureFraud(Cookies cookies) {
        Response response = client.target(getStubIdpUri(Urls.IDP_LOGIN_RESOURCE))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .get();

        Form form = new Form();
        form.param(Urls.LOGIN_FAILURE_STATUS_PARAM, FraudIndicator.DF01.name());
        final Document entity = Jsoup.parse(response.readEntity(String.class));
        final Element csrfElement = entity.getElementById(CSRF_PROTECT_FORM_KEY);
        if(!Objects.isNull(csrfElement)) {
            form.param(CSRF_PROTECT_FORM_KEY, entity.getElementById(CSRF_PROTECT_FORM_KEY).val());
        }

        response = client.target(getStubIdpUri(Urls.IDP_FRAUD_FAILURE_RESOURCE))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .post(Entity.form(form));

        assertThat(response.getStatus()).isEqualTo(200);
        return getSamlResponseFromResponseString(response.readEntity(String.class));
    }

    public void eidasUserLogsIn(Cookies cookies, boolean signAssertions) {
        userLogsIn(cookies, STUB_COUNTRY_FRIENDLY_ID, Urls.EIDAS_LOGIN_RESOURCE, Urls.EIDAS_CONSENT_RESOURCE, signAssertions);
    }

    public void userLogsIn(Cookies cookies, String username) {
        userLogsIn(cookies, username, Urls.IDP_LOGIN_RESOURCE, Urls.IDP_CONSENT_RESOURCE, false);
    }

    private void userLogsIn(Cookies cookies, String username, String loginUrl, String consentUrl, boolean eidasSignAssertions) {
        Response response = client.target(getStubIdpUri(loginUrl))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .get();

        assertThat(response.getStatus()).isEqualTo(200);

        Form form = new Form();
        form.param(Urls.USERNAME_PARAM, username);
        form.param(Urls.PASSWORD_PARAM, "bar");
        form.param(Urls.SUBMIT_PARAM, "SignIn");
        if(eidasSignAssertions) {
            form.param(Urls.SIGN_ASSERTIONS_PARAM, SignAssertions.signAssertions.name());
        }
        final Document entity = Jsoup.parse(response.readEntity(String.class));
        final Element csrfElement = entity.getElementById(CSRF_PROTECT_FORM_KEY);
        if(!Objects.isNull(csrfElement)) {
            form.param(CSRF_PROTECT_FORM_KEY, entity.getElementById(CSRF_PROTECT_FORM_KEY).val());
        }

        response = client.target(getStubIdpUri(loginUrl))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .post(Entity.form(form));

        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getLocation().getPath()).isEqualTo(getStubIdpUri(consentUrl).getPath());
    }

    public Response userConsentsReturnResponse(Cookies cookies, boolean randomize) {
        return userConsentsReturnResponse(cookies, randomize, Urls.IDP_CONSENT_RESOURCE, Optional.empty());
    }

    public Response eidasUserConsentsReturnResponse(Cookies cookies, boolean randomize, String signingAlgorithm) {
        return userConsentsReturnResponse(cookies, randomize, Urls.EIDAS_CONSENT_RESOURCE, Optional.of(signingAlgorithm));
    }

    public String userConsentsReturnSamlResponse(Cookies cookies, boolean randomize) {
        return userConsentsReturnSamlResponse(cookies, randomize, Urls.IDP_CONSENT_RESOURCE, Optional.empty());
    }

    public String eidasUserConsentsReturnSamlResponse(Cookies cookies, boolean randomize, String signingAlgorithm) {
        return userConsentsReturnSamlResponse(cookies, randomize, Urls.EIDAS_CONSENT_RESOURCE, Optional.of(signingAlgorithm));
    }

    private Response userConsentsReturnResponse(Cookies cookies, boolean randomize, String consentUrl, Optional<String> signingAlgorithm) {
        Response response = client.target(getStubIdpUri(consentUrl))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .get();

        assertThat(response.getStatus()).isEqualTo(200);

        Form form = new Form();
        form.param(Urls.SUBMIT_PARAM, "I Agree");
        form.param(Urls.RANDOMISE_PID_PARAM, Boolean.toString(randomize));
        final Document entity = Jsoup.parse(response.readEntity(String.class));
        final Element csrfElement = entity.getElementById(CSRF_PROTECT_FORM_KEY);
        if (!Objects.isNull(csrfElement)) {
            form.param(CSRF_PROTECT_FORM_KEY, entity.getElementById(CSRF_PROTECT_FORM_KEY).val());
        }
        signingAlgorithm.ifPresent(s -> form.param(Urls.SIGNING_ALGORITHM_PARAM, s)); // only for eidas consent POST

        response = client.target(getStubIdpUri(consentUrl))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        return response;
    }

    private String userConsentsReturnSamlResponse(Cookies cookies, boolean randomize, String consentUrl, Optional<String> signingAlgorithm) {
        final Response response = userConsentsReturnResponse(cookies, randomize, consentUrl, signingAlgorithm);
        assertThat(response.getStatus()).isEqualTo(200);
        return getSamlResponseFromResponseString(response.readEntity(String.class));
    }

    public String userViewsTheDebugPage(Cookies cookies) {
        return userViewsTheDebugPage(cookies, getStubIdpUri(Urls.IDP_DEBUG_RESOURCE));
    }

    public String eidasUserViewsTheDebugPage(Cookies cookies) {
        final String page = userViewsTheDebugPage(cookies, getStubIdpUri(Urls.EIDAS_DEBUG_RESOURCE));
        assertThat(page).contains("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier");
        return page;
    }

    private String userViewsTheDebugPage(Cookies cookies, URI debugUrl) {
        Response response = client.target(debugUrl)
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        // we should probably test more things
        final String page = response.readEntity(String.class);
        assertThat(page).contains(cookies.getSessionId());
        return page;
    }

    private IdaKeyStore createValidIdaKeyStore() {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());

        PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_SIGNING_KEY));
        PublicKey publicSigningKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT);

        PrivateKey privateEncKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
        KeyPair encryptionKeyPair = new KeyPair(publicEncKey, privateEncKey);

        X509Certificate certificate = new X509CertificateFactory().createCertificate(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT);
        return new IdaKeyStore(certificate, signingKeyPair, Collections.singletonList(encryptionKeyPair));
    }

    private IdaKeyStore createInvalidIdaKeyStore() {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());

        PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY));
        PublicKey publicSigningKey = publicKeyFactory.createPublicKey(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);

        PrivateKey privateEncKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncKey = publicKeyFactory.createPublicKey(TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT);

        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
        KeyPair encryptionKeyPair = new KeyPair(publicEncKey, privateEncKey);

        X509Certificate certificate = new X509CertificateFactory().createCertificate(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);
        return new IdaKeyStore(certificate, signingKeyPair, Collections.singletonList(encryptionKeyPair));
    }

    public URI getStubIdpUri(String path) {
        return UriBuilder.fromUri("http://localhost:" + port)
                .path(path)
                .build(idpName);
    }
}
