package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.views.AuthenticationFailedView;
import stubsp.stubsp.views.SuccessView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Urls.SUCCESS_RESOURCE)
@Produces(MediaType.TEXT_HTML)
public class SuccessResource {

    @Inject
    public SuccessResource() {
    }

    @GET
    public SuccessView getSuccessView() {
        return new SuccessView();
    }
}
