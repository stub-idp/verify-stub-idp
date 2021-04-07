package stubidp.stubidp.resources.idp;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.shared.csrf.CSRFCheckProtection;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.exceptions.GenericStubIdpException;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.services.SuccessAuthnResponseService;
import stubidp.stubidp.views.ConsentView;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Path(Urls.IDP_CONSENT_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@SessionCookieValueMustExistAsASession
public class ConsentResource {

    private final IdpStubsRepository idpStubsRepository;
    private final IdpSessionRepository sessionRepository;
    private final SuccessAuthnResponseService successAuthnResponseService;
    private final NonSuccessAuthnResponseService nonSuccessAuthnResponseService;
    private final SamlMessageRedirectViewFactory samlMessageRedirectViewFactory;

    public static final String I_AGREE_SUBMIT_VALUE = "I Agree";
    public static final String I_REFUSE_SUBMIT_VALUE = "I Refuse";

    @Inject
    public ConsentResource(
            IdpStubsRepository idpStubsRepository,
            IdpSessionRepository sessionRepository,
            SuccessAuthnResponseService successAuthnResponseService,
            NonSuccessAuthnResponseService nonSuccessAuthnResponseService,
            SamlMessageRedirectViewFactory samlMessageRedirectViewFactory) {
        this.successAuthnResponseService = successAuthnResponseService;
        this.idpStubsRepository = idpStubsRepository;
        this.sessionRepository = sessionRepository;
        this.nonSuccessAuthnResponseService = nonSuccessAuthnResponseService;
        this.samlMessageRedirectViewFactory = samlMessageRedirectViewFactory;
    }

    @GET
    public Response get(
            @PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        IdpSession session = getAndValidateSession(idpName, sessionCookie);

        DatabaseIdpUser idpUser = session.getIdpUser().get();

        List<AuthnContext> requestLevelsOfAssurance = session.getIdaAuthnRequestFromHub().getLevelsOfAssurance();
        AuthnContext userLevelOfAssurance = idpUser.getLevelOfAssurance();
        boolean isUserLOATooLow = requestLevelsOfAssurance.stream().noneMatch(loa -> loa == userLevelOfAssurance);

        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        sessionRepository.updateSession(session.getSessionId(), session.setNewCsrfToken());
        return Response.ok(new ConsentView(idp.getDisplayName(), idp.getFriendlyId(), idp.getAssetId(), idpUser, isUserLOATooLow, userLevelOfAssurance, requestLevelsOfAssurance, session.getCsrfToken())).build();
    }

    private GenericStubIdpException errorResponse(String error) {
        return new GenericStubIdpException(error, Response.Status.BAD_REQUEST);
    }

    @POST
    @CSRFCheckProtection
    public Response consent(
            @Context HttpServletRequest httpServletRequest,
            @PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
            @FormParam(Urls.SUBMIT_PARAM) @NotNull String submitButtonValue,
            @FormParam(Urls.RANDOMISE_PID_PARAM) boolean randomisePid,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {
        
        IdpSession session = getAndValidateSession(idpName, sessionCookie);
        sessionRepository.deleteSession(sessionCookie);

        return switch (submitButtonValue) {
            case I_AGREE_SUBMIT_VALUE -> samlMessageRedirectViewFactory.sendSamlResponse(successAuthnResponseService.getSuccessResponse(randomisePid, httpServletRequest.getRemoteAddr(), idpName, session));
            case I_REFUSE_SUBMIT_VALUE -> samlMessageRedirectViewFactory.sendSamlResponse(nonSuccessAuthnResponseService.generateNoAuthnContext(idpName, session.getIdaAuthnRequestFromHub().getId(), session.getRelayState()));
            default -> throw errorResponse("Invalid button value " + submitButtonValue);
        };
    }

    private IdpSession getAndValidateSession(String idpName, SessionId sessionCookie) {
        if (Objects.isNull(sessionCookie.toString()) || sessionCookie.toString().isBlank()) {
            throw errorResponse("Unable to locate session cookie for " + idpName);
        }

        Optional<IdpSession> session = sessionRepository.get(sessionCookie);
        
        if (session.isEmpty() || session.get().getIdpUser().isEmpty() || session.get().getIdaAuthnRequestFromHub() == null) {
            throw errorResponse("Session is invalid for " + idpName);
        }
        
        return session.get();
    }
}
