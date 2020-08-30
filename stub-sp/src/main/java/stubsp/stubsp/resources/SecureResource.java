package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.filters.RequireValidLogin;
import stubsp.stubsp.services.SecureService;
import stubsp.stubsp.views.SecureView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static stubsp.stubsp.StubSpBinder.SERVICE_NAME;

@Path(Urls.SECURE_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@RequireValidLogin
public class SecureResource {

    private final SecureService secureService;
    private final String serviceName;

    @Inject
    public SecureResource(SecureService secureService,
                          @Named(SERVICE_NAME) String serviceName) {
        this.secureService = secureService;
        this.serviceName = serviceName;
    }

    @GET
    public SecureView getSecureView() {
        return new SecureView();
    }
}
