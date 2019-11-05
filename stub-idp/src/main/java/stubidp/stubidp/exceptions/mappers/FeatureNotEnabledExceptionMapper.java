package stubidp.stubidp.exceptions.mappers;

import org.apache.log4j.Logger;
import stubidp.stubidp.views.FeatureNotEnabledPageView;
import stubidp.stubidp.exceptions.FeatureNotEnabledException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class FeatureNotEnabledExceptionMapper implements ExceptionMapper<FeatureNotEnabledException> {
	private static final Logger LOG = Logger.getLogger(FeatureNotEnabledExceptionMapper.class);
	@Override
	public Response toResponse(FeatureNotEnabledException exception) {
		LOG.error(exception);
		return Response
				.status(Response.Status.PRECONDITION_FAILED)
				.entity(new FeatureNotEnabledPageView())
				.type(MediaType.TEXT_HTML)
				.build();
	}
}
