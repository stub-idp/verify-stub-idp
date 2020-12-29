package stubidp.utils.rest.resources;


import stubidp.utils.rest.common.CommonUrls;
import stubidp.utils.rest.common.ServiceNameDto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(CommonUrls.SERVICE_NAME_ROOT)
public class ServiceNameResource {

    private final ServiceNameDto serviceNameDto;

    public ServiceNameResource(String serviceName) {
        this.serviceNameDto = new ServiceNameDto(serviceName);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceNameDto getServiceName() {
        return serviceNameDto;
    }
}
