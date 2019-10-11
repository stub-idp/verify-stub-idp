package stubidp.stubidp.views;

import com.google.common.net.HttpHeaders;
import stubidp.stubidp.cookies.CookieNames;
import stubidp.stubidp.cookies.HttpOnlyNewCookie;
import stubidp.stubidp.domain.SamlRequest;
import stubidp.stubidp.domain.SamlResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

/*
 * Generate the HTML for a SAML Redirect
 */
public class SamlMessageRedirectViewFactory {
    private final CookieNames cookieNames;

    @Inject
    public SamlMessageRedirectViewFactory(CookieNames cookieNames) {
        this.cookieNames = cookieNames;
    }

    public Response sendSamlResponse(SamlResponse samlResponse) {
        SamlRedirectView samlFormPostingView = getSamlRedirectView(SamlMessageType.SAML_RESPONSE, samlResponse.getSpSSOUrl(), samlResponse.getResponseString(), samlResponse.getRelayState(), Optional.empty());

        return Response.ok(samlFormPostingView)
            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store")
            .header(HttpHeaders.PRAGMA, "no-cache")
            .cookie(new HttpOnlyNewCookie(cookieNames.getSessionCookieName(),"","/","",0,false))
            .build();
    }

    public Response sendSamlRequest(SamlRequest samlRequest) {
        SamlRedirectView samlFormPostingView = getSamlRedirectView(SamlMessageType.SAML_REQUEST, samlRequest.getIdpSSOUrl(), samlRequest.getRequestString(), samlRequest.getRelayState(), Optional.empty());
        return Response.ok(samlFormPostingView)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store")
                .header(HttpHeaders.PRAGMA, "no-cache")
                // FIXME: cookies??
                .build();
    }

    private SamlRedirectView getSamlRedirectView(SamlMessageType samlMessageType, URI targetUri, String samlMessage, String relayState, Optional<Boolean> registration) {
        return new SamlRedirectView(targetUri, samlMessage, samlMessageType, relayState, registration);
    }
}
