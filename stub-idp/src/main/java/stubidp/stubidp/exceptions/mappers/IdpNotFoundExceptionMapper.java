package stubidp.stubidp.exceptions.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.exceptions.IdpNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class IdpNotFoundExceptionMapper implements ExceptionMapper<IdpNotFoundException> {
    private static final Logger LOG = LoggerFactory.getLogger(IdpNotFoundExceptionMapper.class);
    @Override
    public Response toResponse(IdpNotFoundException exception) {
        LOG.error(String.valueOf(exception));
        return Response.status(Response.Status.NOT_FOUND).entity(exception.getMessage()).build();
    }
}
