package stubidp.saml.utils.core.transformers.outbound.decorators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.utils.OpenSAMLRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResponseSignatureCreatorTest extends OpenSAMLRunner {

    private static final String RESPONSE_ID = "response-id";
    private ResponseSignatureCreator responseSignatureCreator;

    @Mock
    private Response response;

    @Mock
    private SignatureFactory signatureFactory;

    @BeforeEach
    public void setup() {
        responseSignatureCreator = new ResponseSignatureCreator(signatureFactory);
    }

    @Test
    public void decorate_shouldGetSignatureAndAssignIt() {
        Response response = mock(Response.class);
        String id = "response-id";
        when(response.getSignatureReferenceID()).thenReturn(id);

        responseSignatureCreator.addUnsignedSignatureTo(response);

        verify(signatureFactory).createSignature(RESPONSE_ID);
    }

    @Test
    public void shouldAssignSignatureToResponse() {
        Signature signature = mock(Signature.class);
        String id = "response-id";
        when(response.getSignatureReferenceID()).thenReturn(id);
        when(signatureFactory.createSignature(id)).thenReturn(signature);
        
        responseSignatureCreator.addUnsignedSignatureTo(response);

        verify(response).setSignature(signature);
    }
}
