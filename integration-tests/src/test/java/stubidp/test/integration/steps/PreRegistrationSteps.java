package stubidp.test.integration.steps;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import stubidp.stubidp.Urls;
import stubidp.test.integration.support.StubIdpAppExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY;

public class PreRegistrationSteps {
    private Response response;
    private final Cookies cookies;
    private String csrfToken;
    private final Client client;
    private final StubIdpAppExtension applicationRule;
    private String responseEntity;

    private static final String IDP_NAME = "stub-idp-demo-one";
    private static final String DISPLAY_NAME = "Stub Idp One Pre-Register";

    public PreRegistrationSteps(Client client, StubIdpAppExtension applicationRule) {
        this.client = client;
        this.applicationRule = applicationRule;
        this.cookies = new Cookies();
    }

    public PreRegistrationSteps userNavigatesTo(String path) {
        this.response = client.target(getUri(path))
                .request()
                .cookie(cookies.getSessionCookie())
                .cookie(cookies.getSecureCookie())
                .get();
        cookies.extractCookies(response);
        return this;
    }

    public PreRegistrationSteps userSuccessfullyNavigatesTo(String path) {
        this.response = client.target(getUri(path))
                .request()
                .cookie(cookies.getSessionCookie())
                .cookie(cookies.getSecureCookie())
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        this.responseEntity = response.readEntity(String.class);
        final Document entity = Jsoup.parse(responseEntity);
        final Element csrfElement = entity.getElementById(CSRF_PROTECT_FORM_KEY);
        if(!Objects.isNull(csrfElement)) {
            csrfToken = entity.getElementById(CSRF_PROTECT_FORM_KEY).val();
        }
        cookies.extractCookies(response);
        return this;
    }

    public PreRegistrationSteps userIsRedirectedTo(String path) {
        return userIsRedirectedTo(getUri(path));
    }

    private PreRegistrationSteps userIsRedirectedTo(URI uri) {
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        String uriString = null;
        uriString = URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8);
        assertThat(response.getLocation().toString()).isEqualTo(uriString);
        return this;
    }

    public PreRegistrationSteps theRedirectIsFollowed() {
        response = client.target(response.getLocation())
                .request()
                .cookie(cookies.getSessionCookie())
                .cookie(cookies.getSecureCookie())
                .get();
        cookies.extractCookies(response);
        responseEntity = response.readEntity(String.class);
        return this;
    }

    public PreRegistrationSteps theResponseStatusIs(Response.Status status) {
        assertThat(response.getStatus()).isEqualTo(status.getStatusCode());
        return this;
    }

    public PreRegistrationSteps userSubmitsForm(Form form, String path) {
        return postFormTo(form, path);
    }

    public PreRegistrationSteps userSubmitsFormTo(Form form, URI uri) {
        return postFormTo(form, uri);
    }

    public PreRegistrationSteps clientPostsFormData(Form form, String path) {
        return postFormTo(form, path);
    }

    private PreRegistrationSteps postFormTo(Form form, String path) {
        return postFormTo(form, getUri(path));
    }

    private PreRegistrationSteps postFormTo(Form form, URI uri){
        response = client.target(uri)
                .request()
                .cookie(cookies.getSessionCookie())
                .cookie(cookies.getSecureCookie())
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        cookies.extractCookies(response);
        return this;
    }

    public PreRegistrationSteps responseContains(String ... content) {
        Arrays.stream(content).forEach(string -> assertThat(responseEntity).contains(string));
        return this;
    }

    public Cookies getCookies() {
        return this.cookies;
    }

    private URI getUri(String path) {
        return UriBuilder.fromUri("http://localhost:" + applicationRule.getLocalPort())
                .path(path)
                .buildFromMap(Map.of(Urls.IDP_ID_PARAM, IDP_NAME));
    }

    public String getCsrfToken() {
        return csrfToken;
    }
}
