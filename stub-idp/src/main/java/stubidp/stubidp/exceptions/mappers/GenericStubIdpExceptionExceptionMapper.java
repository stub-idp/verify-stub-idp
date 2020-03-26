package stubidp.stubidp.exceptions.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.views.ErrorPageView;
import stubidp.stubidp.exceptions.GenericStubIdpException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class GenericStubIdpExceptionExceptionMapper implements ExceptionMapper<GenericStubIdpException> {
    private static final Logger LOG = LoggerFactory.getLogger(GenericStubIdpExceptionExceptionMapper.class);
    @Override
    public Response toResponse(GenericStubIdpException exception) {
        LOG.error(String.valueOf(exception));
        return Response
                .status(exception.getResponseStatus().orElse(Response.Status.INTERNAL_SERVER_ERROR))
                .entity(new ErrorPageView())
                .type(MediaType.TEXT_HTML)
                .build();
    }
}
