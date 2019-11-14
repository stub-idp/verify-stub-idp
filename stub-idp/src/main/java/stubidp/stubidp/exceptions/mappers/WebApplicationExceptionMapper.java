package stubidp.stubidp.exceptions.mappers;

import org.apache.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger LOG = Logger.getLogger(WebApplicationExceptionMapper.class);
    @Override
    public Response toResponse(WebApplicationException exception) {
        LOG.error(exception);
        return exception.getResponse();
    }
}
