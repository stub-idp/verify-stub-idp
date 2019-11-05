package stubidp.stubidp.exceptions.mappers;

import org.apache.log4j.Logger;
import stubidp.stubidp.views.ErrorPageView;
import stubidp.stubidp.exceptions.SessionSerializationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class SessionSerializationExceptionMapper implements ExceptionMapper<SessionSerializationException> {
	private static final Logger LOG = Logger.getLogger(SessionSerializationExceptionMapper.class);
	@Override
	public Response toResponse(SessionSerializationException exception) {
		LOG.error(exception);
		return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(new ErrorPageView())
				.type(MediaType.TEXT_HTML)
				.build();
	}
}
