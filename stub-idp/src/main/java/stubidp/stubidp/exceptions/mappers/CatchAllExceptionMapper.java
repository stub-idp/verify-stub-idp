package stubidp.stubidp.exceptions.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.views.ErrorPageView;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class CatchAllExceptionMapper implements ExceptionMapper<RuntimeException> {
    private static final Logger LOG = LoggerFactory.getLogger(CatchAllExceptionMapper.class);
    @Override
    public Response toResponse(RuntimeException exception) {
        LOG.error(String.valueOf(exception));
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorPageView())
                .type(MediaType.TEXT_HTML)
                .build();
    }
}
