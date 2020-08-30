package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.views.AuthenticationFailedView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Urls.AUTHENTICATION_FAILURE_RESOURCE)
@Produces(MediaType.TEXT_HTML)
public class AuthenticationFailedResource {

    @Inject
    public AuthenticationFailedResource() {
    }

    @GET
    public AuthenticationFailedView getAuthenticationFailedView() {
        return new AuthenticationFailedView();
    }
}
