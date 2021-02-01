package uk.gov.ida.rp.testrp.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;

import javax.ws.rs.core.Response;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HeadlessRpResourceTest {

    private HeadlessRpResource resource;

    @Mock
    private AuthnRequestSenderHandler authnRequestSenderHandler;
    @Mock
    private Function<String, InboundResponseFromHub> samlResponseDeserialiser;
    @Mock
    private InboundResponseFromHub inboundResponseFromHub;

    @BeforeEach
    public void setUp() {
        this.resource = new HeadlessRpResource(samlResponseDeserialiser, authnRequestSenderHandler);
    }

    @Test
    public void postingNoAuthContext_shouldReturnSadFace(){
        String xml = "Some Saml";
        when(samlResponseDeserialiser.apply(xml)).thenReturn(inboundResponseFromHub);
        when(inboundResponseFromHub.getStatus()).thenReturn(TransactionIdaStatus.NoAuthenticationContext);

        Response response = this.resource.doLogin(xml);

        assertThat(response.getEntity().toString()).contains("Headless Failed Log In");
    }
}
