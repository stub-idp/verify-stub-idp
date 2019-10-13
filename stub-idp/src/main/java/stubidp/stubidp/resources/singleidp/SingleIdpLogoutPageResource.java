package stubidp.stubidp.resources.singleidp;

import stubidp.shared.cookies.HttpOnlyNewCookie;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.IdpSessionRepository;
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
import javax.ws.rs.core.UriBuilder;

@Path(Urls.SINGLE_IDP_LOGOUT_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@SessionCookieValueMustExistAsASession
public class SingleIdpLogoutPageResource {

    private IdpSessionRepository idpSessionRepository;

    @Inject
    public SingleIdpLogoutPageResource(IdpSessionRepository idpSessionRepository) {
        this.idpSessionRepository = idpSessionRepository;
    }

    @GET
    public Response get(@PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
                        @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) SessionId session) {

        idpSessionRepository.deleteSession(session);
        return Response.seeOther(UriBuilder.fromPath(Urls.SINGLE_IDP_HOMEPAGE_RESOURCE).build(idpName))
                .cookie(new HttpOnlyNewCookie(
                        StubIdpCookieNames.SESSION_COOKIE_NAME,
                        "",
                        "/",
                        "",
                        0,
                        false
                        ))
                .build();
    }
}
