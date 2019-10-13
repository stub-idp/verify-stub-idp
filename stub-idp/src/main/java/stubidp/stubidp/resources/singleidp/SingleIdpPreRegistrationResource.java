package stubidp.stubidp.resources.singleidp;

import stubidp.shared.cookies.CookieFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.views.CancelPreRegistrationPageView;
import stubidp.stubidp.views.ErrorMessageType;
import stubidp.stubidp.views.RegistrationPageView;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

@Path(Urls.SINGLE_IDP_PRE_REGISTER_RESOURCE)
@Produces(MediaType.TEXT_HTML)
public class SingleIdpPreRegistrationResource {

    private final IdpStubsRepository idpStubsRepository;
    private final CookieFactory cookieFactory;
    private final IdpSessionRepository idpSessionRepository;

    @Inject
    public SingleIdpPreRegistrationResource(
            IdpStubsRepository idpStubsRepository, CookieFactory cookieFactory, IdpSessionRepository idpSessionRepository) {
        this.idpStubsRepository = idpStubsRepository;
        this.cookieFactory = cookieFactory;
        this.idpSessionRepository = idpSessionRepository;
    }

    @GET
    @Path(Urls.SINGLE_IDP_PRE_REGISTER_CANCEL_PATH)
    @Produces(MediaType.TEXT_HTML)
    public Response getPreRegisterCancel(
            @PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
            @QueryParam(Urls.ERROR_MESSAGE_PARAM) Optional<ErrorMessageType> errorMessage) {

        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        return Response.ok(new CancelPreRegistrationPageView(idp.getDisplayName(), idp.getFriendlyId(), errorMessage.orElse(ErrorMessageType.NO_ERROR).getMessage(), idp.getAssetId())).build();
    }

    @GET
    public Response getPreRegister(
            @PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
            @QueryParam(Urls.ERROR_MESSAGE_PARAM) Optional<ErrorMessageType> errorMessage) {

        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        return createSessionAndShowRegistrationForm(idp, errorMessage);
    }

    private Response createSessionAndShowRegistrationForm(Idp idp, Optional<ErrorMessageType> errorMessage){
        final IdpSession session = new IdpSession(new SessionId(UUID.randomUUID().toString()));
        final SessionId sessionId = idpSessionRepository.createSession(session);
        idpSessionRepository.updateSession(session.getSessionId(), session.setNewCsrfToken());

        return Response.ok().entity(new RegistrationPageView(idp.getDisplayName(), idp.getFriendlyId(), errorMessage.orElse(ErrorMessageType.NO_ERROR).getMessage(), idp.getAssetId(), true, session.getCsrfToken()))
                .cookie(cookieFactory.createSessionIdCookie(sessionId))
                .cookie(cookieFactory.createSecureCookieWithSecurelyHashedValue(sessionId))
                .build();
    }
}
