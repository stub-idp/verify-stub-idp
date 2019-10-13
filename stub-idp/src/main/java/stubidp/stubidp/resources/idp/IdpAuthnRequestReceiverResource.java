package stubidp.stubidp.resources.idp;

import io.prometheus.client.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.shared.cookies.CookieFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.domain.IdpLanguageHint;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.services.AuthnRequestReceiverService;
import stubidp.stubidp.services.AuthnRequestReceiverService.SessionCreated;
import stubidp.stubidp.services.IdpUserService;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
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
import java.util.Set;
import java.util.UUID;

import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.IS_SECURE_COOKIE_ENABLED;

@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.TEXT_HTML)
@Path(Urls.IDP_SAML2_SSO_RESOURCE)
public class IdpAuthnRequestReceiverResource {

    private static final Logger LOG = LoggerFactory.getLogger(IdpAuthnRequestReceiverResource.class);

    private static final String STUBIDP_VERIFY_RECEIVED_AUTHN_REQUESTS_TOTAL = "stubidp_verify_receivedAuthnRequests_total";

    public static final Counter receivedVerifyAuthnRequests = Counter.build()
            .name(STUBIDP_VERIFY_RECEIVED_AUTHN_REQUESTS_TOTAL)
            .help("Number of received verify authn requests.")
            .register();

    private final AuthnRequestReceiverService authnRequestReceiverService;
    private final CookieFactory cookieFactory;
    private final Boolean isSecureCookieEnabled;
    private final IdpSessionRepository idpSessionRepository;
    private final IdpUserService idpUserService;

    @Inject
    public IdpAuthnRequestReceiverResource(AuthnRequestReceiverService authnRequestReceiverService,
                                           CookieFactory cookieFactory,
                                           @Named(IS_SECURE_COOKIE_ENABLED) Boolean isSecureCookieEnabled,
                                           IdpSessionRepository idpSessionRepository,
                                           IdpUserService idpUserService) {
        this.authnRequestReceiverService = authnRequestReceiverService;
        this.cookieFactory = cookieFactory;
        this.isSecureCookieEnabled = isSecureCookieEnabled;
        this.idpSessionRepository = idpSessionRepository;
        this.idpUserService = idpUserService;
    }

    @POST
    public Response handlePost(
            @PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
            @FormParam(Urls.SAML_REQUEST_PARAM) @NotNull String samlRequest,
            @FormParam(Urls.HINTS_PARAM) Set<String> idpHints,
            @FormParam(Urls.REGISTRATION_PARAM) Optional<Boolean> registration,
            @FormParam(Urls.RELAY_STATE_PARAM) String relayState,
            @FormParam(Urls.LANGUAGE_HINT_PARAM) Optional<IdpLanguageHint> languageHint,
            @FormParam(Urls.SINGLE_IDP_JOURNEY_ID_PARAM) Optional<UUID> singleIdpJourneyId,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) SessionId sessionCookie) {
        LOG.debug("Received request for idp {} from HUB", idpName);

        receivedVerifyAuthnRequests.inc();

        final SessionCreated sessionCreated = authnRequestReceiverService.handleAuthnRequest(idpName, samlRequest, idpHints, registration, relayState, languageHint, singleIdpJourneyId);
        if (sessionCookie != null) {
            Optional<IdpSession> preRegSession = idpSessionRepository.get(sessionCookie);
            if (preRegSession.isPresent() && preRegSession.get().getIdpUser().isPresent()) {
                try {
                    idpUserService.attachIdpUserToSession(preRegSession.get().getIdpUser(), sessionCreated.getIdpSessionId());
                    idpSessionRepository.deleteSession(preRegSession.get().getSessionId());
                } catch (InvalidUsernameOrPasswordException | InvalidSessionIdException e) {
                    throw new RuntimeException(e);
                }
            }
        }
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

        return cookies.toArray(new NewCookie[cookies.size()]);
    }
}
