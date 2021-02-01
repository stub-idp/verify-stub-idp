package uk.gov.ida.rp.testrp.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.utils.rest.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.controllogic.AuthnResponseReceiverHandler;
import uk.gov.ida.rp.testrp.domain.ResponseFromHub;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthnResponseReceiverResourceTest {

    @Mock
    private AuthnResponseReceiverHandler authnResponseReceiverHandler;
    @Mock
    private TestRpConfiguration testRpConfiguration;

    @Test
    public void assertCorrectRedirectUrlWhenReceivingNoAuthnContextResponseFromHub() {
        String samlResponse = "no-authn-context-saml-response";
        ResponseFromHub responseFromHub = new ResponseFromHub(
                TransactionIdaStatus.NoAuthenticationContext,
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        when(authnResponseReceiverHandler.handleResponse(eq(samlResponse), any())).thenReturn(responseFromHub);

        AuthnResponseReceiverResource resource = new AuthnResponseReceiverResource(authnResponseReceiverHandler, testRpConfiguration);
        Response response = resource.doLogin(samlResponse, SessionId.createNewSessionId(), null);
        assertThat(response.getLocation().toString()).isEqualTo("/test-rp?errorCode=NoAuthenticationContext");
    }
}
