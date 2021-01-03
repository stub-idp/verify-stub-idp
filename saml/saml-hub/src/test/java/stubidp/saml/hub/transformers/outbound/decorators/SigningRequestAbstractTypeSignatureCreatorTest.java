package stubidp.saml.hub.transformers.outbound.decorators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.security.SignatureFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SigningRequestAbstractTypeSignatureCreatorTest {

    private SigningRequestAbstractTypeSignatureCreator<AttributeQuery> signatureCreator;
    @Mock
    private SignatureFactory signatureFactory;

    private static final String id = "response-id";

    @BeforeEach
    void setup() {
        signatureCreator = new SigningRequestAbstractTypeSignatureCreator<>(signatureFactory);
    }

    @Test
    void decorate_shouldGetSignatureAndAssignIt() {
        AttributeQuery response = mock(AttributeQuery.class);
        when(response.getID()).thenReturn(id);

        signatureCreator.addUnsignedSignatureTo(response);

        verify(signatureFactory).createSignature(id);
    }

    @Test
    void decorate_shouldAssignSignatureToResponse() {
        AttributeQuery response = mock(AttributeQuery.class);
        Signature signature = mock(Signature.class);
        when(response.getID()).thenReturn(id);
        when(signatureFactory.createSignature(id)).thenReturn(signature);

        signatureCreator.addUnsignedSignatureTo(response);

        verify(response).setSignature(signature);
    }
}
