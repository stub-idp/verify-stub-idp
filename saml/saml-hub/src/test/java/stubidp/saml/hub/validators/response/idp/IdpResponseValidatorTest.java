package stubidp.saml.hub.validators.response.idp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.hub.domain.CountryAuthenticationStatus;
import stubidp.saml.hub.validators.response.idp.components.EncryptedResponseFromIdpValidator;
import stubidp.saml.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdpResponseValidatorTest {

    @Mock
    private SamlResponseSignatureValidator samlResponseSignatureValidator;
    @Mock
    private AssertionDecrypter assertionDecrypter;
    @Mock
    private SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;
    @Mock
    private EncryptedResponseFromIdpValidator<CountryAuthenticationStatus.Status> encryptedResponseFromIdpValidator;
    @Mock
    private DestinationValidator responseDestinationValidator;
    @Mock
    private ResponseAssertionsFromIdpValidator responseAssertionsFromIdpValidator;
    @Mock
    private Response response;

    private IdpResponseValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new IdpResponseValidator(
            samlResponseSignatureValidator,
            assertionDecrypter,
            samlAssertionsSignatureValidator,
            encryptedResponseFromIdpValidator,
            responseDestinationValidator,
            responseAssertionsFromIdpValidator);
    }

    @Test
    public void shouldValidateResponseIsEncrypted() {
        validator.validate(response);
        verify(encryptedResponseFromIdpValidator).validate(response);
    }

    @Test
    public void shouldValidateResponseDestination() {
        validator.validate(response);
        verify(responseDestinationValidator).validate(response.getDestination());
    }

    @Test
    public void shouldValidateSamlResponseSignature() {
        validator.validate(response);
        verify(samlResponseSignatureValidator).validate(response, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldValidateSamlAssertionSignature() {
        Assertion assertion = mock(Assertion.class);
        List<Assertion> assertions = List.of(assertion);
        ValidatedResponse validatedResponse = mock(ValidatedResponse.class);

        when(samlResponseSignatureValidator.validate(response, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(validatedResponse);
        when(assertionDecrypter.decryptAssertions(validatedResponse)).thenReturn(assertions);

        validator.validate(response);

        verify(samlAssertionsSignatureValidator).validate(assertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }
}
