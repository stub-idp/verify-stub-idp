package stubidp.stubidp.exceptions.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger LOG = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);
    @Override
    public Response toResponse(WebApplicationException exception) {
        LOG.error(String.valueOf(exception));
        return exception.getResponse();
    }
}
