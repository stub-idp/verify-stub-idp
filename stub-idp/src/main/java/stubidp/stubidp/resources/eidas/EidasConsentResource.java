package stubidp.stubidp.resources.eidas;

import com.google.common.base.Strings;
import stubidp.stubidp.csrf.CSRFCheckProtection;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.domain.EidasUser;
import stubidp.stubidp.domain.SamlResponse;
import stubidp.stubidp.exceptions.GenericStubIdpException;
import stubidp.stubidp.exceptions.InvalidEidasSchemeException;
import stubidp.stubidp.exceptions.InvalidSigningAlgorithmException;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountry;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.stubidp.views.EidasConsentView;
import stubidp.stubidp.views.SamlResponseRedirectViewFactory;
import stubidp.utils.rest.common.SessionId;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.CookieNames;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.services.EidasAuthnResponseService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static stubidp.stubidp.StubIdpEidasBinder.RSASHA256_EIDAS_AUTHN_RESPONSE_SERVICE;
import static stubidp.stubidp.StubIdpEidasBinder.RSASSAPSS_EIDAS_AUTHN_RESPONSE_SERVICE;

@Path(Urls.EIDAS_CONSENT_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@SessionCookieValueMustExistAsASession
@CSRFCheckProtection
public class EidasConsentResource {

    private final EidasSessionRepository sessionRepository;
    private final StubCountryRepository stubCountryRepository;
    private final EidasAuthnResponseService rsaSha256AuthnResponseService;
    private final EidasAuthnResponseService rsaSsaPssAuthnResponseService;
    private final SamlResponseRedirectViewFactory samlResponseRedirectViewFactory;

    public static final String RSASHA_256 = "rsasha256";
    public static final String RSASSA_PSS = "rsassa-pss";

    @Inject
    public EidasConsentResource(
            EidasSessionRepository sessionRepository,
            @Named(RSASHA256_EIDAS_AUTHN_RESPONSE_SERVICE) EidasAuthnResponseService rsaSha256AuthnResponseService,
            @Named(RSASSAPSS_EIDAS_AUTHN_RESPONSE_SERVICE) EidasAuthnResponseService rsaSsaPssAuthnResponseService,
            SamlResponseRedirectViewFactory samlResponseRedirectViewFactory,
            StubCountryRepository stubCountryRepository) {
        this.rsaSha256AuthnResponseService = rsaSha256AuthnResponseService;
        this.rsaSsaPssAuthnResponseService = rsaSsaPssAuthnResponseService;
        this.sessionRepository = sessionRepository;
        this.samlResponseRedirectViewFactory = samlResponseRedirectViewFactory;
        this.stubCountryRepository = stubCountryRepository;
    }

    @GET
    public Response get(
            @PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeId,
            @CookieParam(CookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        final Optional<EidasScheme> eidasScheme = EidasScheme.fromString(schemeId);
        if(!eidasScheme.isPresent()) {
            throw new InvalidEidasSchemeException();
        }

        EidasSession session = getAndValidateSession(schemeId, sessionCookie, false);

        EidasUser eidasUser = session.getEidasUser().get();
        StubCountry stubCountry = stubCountryRepository.getStubCountryWithFriendlyId(eidasScheme.get());

        sessionRepository.updateSession(session.getSessionId(), session.setNewCsrfToken());

        return Response.ok(new EidasConsentView(stubCountry.getDisplayName(), stubCountry.getFriendlyId(), stubCountry.getAssetId(), eidasUser, session.getCsrfToken())).build();
    }

    @POST
    public Response consent(
            @PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeId,
            @FormParam(Urls.SIGNING_ALGORITHM_PARAM) @NotNull String signingAlgorithm,
            @FormParam(Urls.SUBMIT_PARAM) @NotNull String submitButtonValue,
            @CookieParam(CookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        if(!EidasScheme.fromString(schemeId).isPresent()) {
            throw new InvalidEidasSchemeException();
        }

        EidasAuthnResponseService successAuthnResponseService;
        if (signingAlgorithm.equals(RSASHA_256)) {
            successAuthnResponseService = rsaSha256AuthnResponseService;
        } else if (signingAlgorithm.equals(RSASSA_PSS)) {
            successAuthnResponseService = rsaSsaPssAuthnResponseService;
        } else {
            throw new InvalidSigningAlgorithmException(signingAlgorithm);
        }

        EidasSession session = getAndValidateSession(schemeId, sessionCookie, true);

        SamlResponse samlResponse = successAuthnResponseService.getSuccessResponse(session, schemeId);
        return samlResponseRedirectViewFactory.sendSamlMessage(samlResponse);
    }

    private EidasSession getAndValidateSession(String schemeId, SessionId sessionCookie, boolean shouldDelete) {
        if (sessionCookie == null || Strings.isNullOrEmpty(sessionCookie.toString())) {
            throw errorResponse("Unable to locate session cookie for " + schemeId);
        }

        Optional<EidasSession> session = shouldDelete ? sessionRepository.deleteAndGet(sessionCookie) : sessionRepository.get(sessionCookie);

        if (!session.isPresent() || !session.get().getEidasUser().isPresent() || session.get().getEidasAuthnRequest() == null) {
            throw errorResponse("Session is invalid for " + schemeId);
        }

        return session.get();
    }

    private GenericStubIdpException errorResponse(String error) {
        return new GenericStubIdpException(error, Response.Status.BAD_REQUEST);
    }
}
