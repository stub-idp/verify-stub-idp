package stubidp.utils.rest.filters;

import stubidp.utils.rest.configuration.ServiceStatus;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class ConnectionCloseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        ServiceStatus serviceStatus = ServiceStatus.getInstance();
        if (!serviceStatus.isServerStatusOK()) {
            responseContext.getHeaders().add("Connection", "close");
        }
    }
}
