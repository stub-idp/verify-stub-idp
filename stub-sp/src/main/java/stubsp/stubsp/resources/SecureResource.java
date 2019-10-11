package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.filters.RequireValidLogin;
import stubsp.stubsp.services.SecureService;
import stubsp.stubsp.views.SecureView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Urls.SECURE_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@RequireValidLogin
public class SecureResource {

    private final SecureService secureService;

    @Inject
    public SecureResource(SecureService secureService) {

        this.secureService = secureService;
    }

    @GET
    public SecureView getSecureView() {
        return null;
    }
}
