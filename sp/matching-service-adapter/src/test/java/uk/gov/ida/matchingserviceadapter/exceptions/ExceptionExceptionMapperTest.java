package uk.gov.ida.matchingserviceadapter.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.rest.common.ExceptionType;
import stubidp.utils.rest.exceptions.ApplicationException;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExceptionExceptionMapperTest {

    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Test
    public void shouldNotRespondWithTheBodyOfTheUpstreamError() {
        when(configuration.getReturnStackTraceInErrorResponse()).thenReturn(false);
        ExceptionExceptionMapper exceptionExceptionMapper = new ExceptionExceptionMapper(configuration);

        String causeMessage = "my message";
        ApplicationException unauditedException = ApplicationException.createUnauditedException(ExceptionType.CLIENT_ERROR, causeMessage, new Exception());
        Response response = exceptionExceptionMapper.toResponse(unauditedException);

        String responseBody = (String) response.getEntity();
        assertThat(responseBody).contains("stubidp.utils.rest.exceptions.ApplicationException");
        assertThat(responseBody).doesNotContain(causeMessage);
    }

    @Test
    public void shouldRespondWithTheBodyOfTheUpstreamErrorWhenReturningCauseStackTrace() {
        when(configuration.getReturnStackTraceInErrorResponse()).thenReturn(true);
        ExceptionExceptionMapper exceptionExceptionMapper = new ExceptionExceptionMapper(configuration);

        String causeMessage = "my message";
        ApplicationException unauditedException = ApplicationException.createUnauditedException(ExceptionType.CLIENT_ERROR, causeMessage, new Exception());
        Response response = exceptionExceptionMapper.toResponse(unauditedException);

        String responseBody = (String) response.getEntity();
        assertThat(responseBody).contains("stubidp.utils.rest.exceptions.ApplicationException");
        assertThat(responseBody).contains(causeMessage);
    }
}
