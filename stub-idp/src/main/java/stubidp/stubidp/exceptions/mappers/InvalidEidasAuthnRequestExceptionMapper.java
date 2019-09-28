package stubidp.stubidp.exceptions.mappers;

import io.prometheus.client.Counter;
import org.apache.log4j.Logger;
import stubidp.stubidp.exceptions.InvalidEidasAuthnRequestException;
import stubidp.stubidp.views.ErrorPageView;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class InvalidEidasAuthnRequestExceptionMapper implements ExceptionMapper<InvalidEidasAuthnRequestException> {

    private static final String STUBIDP_EIDAS_INVALID_AUTHN_REQUESTS_RECEIVED_TOTAL = "stubidp_eidas_invalid_AuthnRequests_received_total";
    public static final Counter invalidEidasAuthnRequests = Counter.build()
            .name(STUBIDP_EIDAS_INVALID_AUTHN_REQUESTS_RECEIVED_TOTAL)
            .help("Number of invalid eidas authn requests received.")
            .register();

    private static final Logger LOG = Logger.getLogger(InvalidEidasAuthnRequestException.class);

    @Override
    public Response toResponse(InvalidEidasAuthnRequestException exception) {
        LOG.error(exception);
        invalidEidasAuthnRequests.inc();
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorPageView())
                .build();
    }
}
