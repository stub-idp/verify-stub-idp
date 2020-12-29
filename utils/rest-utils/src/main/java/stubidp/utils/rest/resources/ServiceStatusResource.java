package stubidp.utils.rest.resources;

import javax.inject.Inject;
import org.apache.http.HttpStatus;
import stubidp.utils.rest.configuration.ServiceStatus;
import stubidp.utils.rest.common.CommonUrls;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path(CommonUrls.SERVICE_STATUS)
public class ServiceStatusResource {

    private final ServiceStatus serviceStatus;

    @Inject
    public ServiceStatusResource() {

        this.serviceStatus = ServiceStatus.getInstance();
    }

    @GET
    public Response isOnline(){
        if (serviceStatus.isServerStatusOK()){
            return Response.ok().build();
        } else {
            return Response.status(HttpStatus.SC_SERVICE_UNAVAILABLE).build();
        }
    }
}
