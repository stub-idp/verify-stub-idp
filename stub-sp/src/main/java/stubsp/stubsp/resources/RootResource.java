package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.services.RootService;
import stubsp.stubsp.views.RootView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static stubsp.stubsp.StubSpBinder.SERVICE_NAME;

@Path(Urls.ROOT_RESOURCE)
@Produces(MediaType.TEXT_HTML)
public class RootResource {

    private final RootService rootService;
    private final String serviceName;

    @Inject
    public RootResource(RootService rootService, @Named(SERVICE_NAME) String serviceName) {

        this.rootService = rootService;
        this.serviceName = serviceName;
    }

    @GET
    public RootView getRootView() {
        return new RootView(serviceName);
    }
}
