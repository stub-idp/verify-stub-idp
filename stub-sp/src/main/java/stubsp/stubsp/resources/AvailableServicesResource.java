package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.domain.AvailableServiceDto;
import stubsp.stubsp.services.AvailableServicesService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path(Urls.AVAILABLE_SERVICE_RESOURCE)
@Produces(MediaType.APPLICATION_JSON)
public class AvailableServicesResource {

    private final AvailableServicesService availableServicesService;

    @Inject
    public AvailableServicesResource(AvailableServicesService availableServicesService) {

        this.availableServicesService = availableServicesService;
    }

    @GET
    public List<AvailableServiceDto> getAvailableServices() {
        return availableServicesService.getAvailableServices();
    }
}
