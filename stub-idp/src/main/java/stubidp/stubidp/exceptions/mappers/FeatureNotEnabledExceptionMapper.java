package stubidp.stubidp.exceptions.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.views.FeatureNotEnabledPageView;
import stubidp.stubidp.exceptions.FeatureNotEnabledException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class FeatureNotEnabledExceptionMapper implements ExceptionMapper<FeatureNotEnabledException> {
	private static final Logger LOG = LoggerFactory.getLogger(FeatureNotEnabledExceptionMapper.class);
	@Override
	public Response toResponse(FeatureNotEnabledException exception) {
		LOG.error(String.valueOf(exception));
		return Response
				.status(Response.Status.PRECONDITION_FAILED)
				.entity(new FeatureNotEnabledPageView())
				.type(MediaType.TEXT_HTML)
				.build();
	}
}
