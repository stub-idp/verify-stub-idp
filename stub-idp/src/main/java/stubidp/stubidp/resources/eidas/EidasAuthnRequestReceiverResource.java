package stubidp.stubidp.resources.eidas;

import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.shared.cookies.CookieFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.domain.IdpLanguageHint;
import stubidp.stubidp.exceptions.InvalidEidasSchemeException;
import stubidp.stubidp.services.AuthnRequestReceiverService;
import stubidp.stubidp.services.AuthnRequestReceiverService.SessionCreated;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.IS_SECURE_COOKIE_ENABLED;

@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.TEXT_HTML)
@Path(Urls.EIDAS_SAML2_SSO_RESOURCE)
public class EidasAuthnRequestReceiverResource {

    private static final Logger LOG = LoggerFactory.getLogger(EidasAuthnRequestReceiverResource.class);

    private static final String STUBIDP_EIDAS_RECEIVED_AUTHN_REQUESTS_TOTAL = "stubidp_eidas_receivedAuthnRequests_total";

    public static final Counter receivedEidasAuthnRequests = Counter.build()
            .name(STUBIDP_EIDAS_RECEIVED_AUTHN_REQUESTS_TOTAL)
            .help("Number of received eidas authn requests.")
            .register();

    private final AuthnRequestReceiverService authnRequestReceiverService;
    private final CookieFactory cookieFactory;
    private final Boolean isSecureCookieEnabled;

    @Inject
    public EidasAuthnRequestReceiverResource(AuthnRequestReceiverService authnRequestReceiverService,
                                             CookieFactory cookieFactory,
                                             @Named(IS_SECURE_COOKIE_ENABLED) Boolean isSecureCookieEnabled) {
        this.authnRequestReceiverService = authnRequestReceiverService;
        this.cookieFactory = cookieFactory;
        this.isSecureCookieEnabled = isSecureCookieEnabled;
    }

    @POST
    public Response handleEidasPost(
            @PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeId,
            @FormParam(Urls.SAML_REQUEST_PARAM) @NotNull String samlRequest,
            @FormParam(Urls.RELAY_STATE_PARAM) String relayState,
            @FormParam(Urls.LANGUAGE_HINT_PARAM) Optional<IdpLanguageHint> languageHint) {
        LOG.debug("Received request for country {} from HUB", schemeId);

        receivedEidasAuthnRequests.inc();

        if(EidasScheme.fromString(schemeId).isEmpty()) {
            throw new InvalidEidasSchemeException();
        }

        final SessionCreated sessionCreated = authnRequestReceiverService.handleEidasAuthnRequest(schemeId, samlRequest, relayState, languageHint);

        return Response.seeOther(sessionCreated.getNextLocation())
                .cookie(getCookies(sessionCreated))
                .build();
    }

    private NewCookie[] getCookies(SessionCreated sessionCreated) {
        List<NewCookie> cookies = new ArrayList<>();
        if(isSecureCookieEnabled) {
            cookies.add(cookieFactory.createSecureCookieWithSecurelyHashedValue(sessionCreated.getIdpSessionId()));
        }
        cookies.add(cookieFactory.createSessionIdCookie(sessionCreated.getIdpSessionId()));

        return cookies.toArray(new NewCookie[0]);
    }
}
