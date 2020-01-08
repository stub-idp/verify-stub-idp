package stubidp.stubidp.resources.idp;

import com.google.common.base.Strings;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.exceptions.GenericStubIdpException;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.views.DebugPageView;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static java.text.MessageFormat.format;

@Path(Urls.IDP_DEBUG_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@SessionCookieValueMustExistAsASession
public class DebugPageResource {

    private final IdpStubsRepository idpStubsRepository;
    private final IdpSessionRepository sessionRepository;

    @Inject
    public DebugPageResource(
            IdpStubsRepository idpStubsRepository,
            IdpSessionRepository sessionRepository) {
        this.idpStubsRepository = idpStubsRepository;
        this.sessionRepository = sessionRepository;
    }

    @GET
    public Response get(
            @PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        if (Strings.isNullOrEmpty(sessionCookie.toString())) {
            throw new GenericStubIdpException(format("Unable to locate session cookie for " + idpName), Response.Status.BAD_REQUEST);
        }

        Optional<IdpSession> session = sessionRepository.get(sessionCookie);

        if (session.isEmpty()) {
            throw new GenericStubIdpException(format("Session is invalid for " + idpName), Response.Status.BAD_REQUEST);
        }

        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        return Response.ok(new DebugPageView(idp.getDisplayName(), idp.getFriendlyId(), idp.getAssetId(), session.get())).build();
    }

}
