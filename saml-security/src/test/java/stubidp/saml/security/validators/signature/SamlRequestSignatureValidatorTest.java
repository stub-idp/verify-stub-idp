package stubidp.saml.security.validators.signature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationResponse;
import stubidp.saml.security.SamlMessageSignatureValidator;
import stubidp.saml.security.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.security.errors.SamlTransformationErrorFactory.unableToDecrypt;

public class SamlRequestSignatureValidatorTest extends OpenSAMLRunner {

    private SamlMessageSignatureValidator samlMessageSignatureValidator;
    private SamlRequestSignatureValidator<AuthnRequest> requestSignatureValidator;
    private AuthnRequest authnRequest;

    @BeforeEach
    public void setUp() throws Exception {
        samlMessageSignatureValidator = mock(SamlMessageSignatureValidator.class);
        requestSignatureValidator = new SamlRequestSignatureValidator<>(samlMessageSignatureValidator);
        authnRequest = mock(AuthnRequest.class);
    }

    @Test
    public void validate_shouldDoNothingIfAuthnRequestSignatureIsValid() {
        when(samlMessageSignatureValidator.validate(authnRequest, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(SamlValidationResponse.aValidResponse());

        requestSignatureValidator.validate(authnRequest, SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        verify(samlMessageSignatureValidator).validate(authnRequest, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void validate_shouldThrowExceptionIfAuthnRequestSignatureIsInvalid() {
        SamlValidationResponse invalidResponse = SamlValidationResponse.anInvalidResponse(unableToDecrypt("Error"));
        when(samlMessageSignatureValidator.validate(authnRequest, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(invalidResponse);

        final SamlTransformationErrorException exception = Assertions.assertThrows(SamlTransformationErrorException.class, () -> requestSignatureValidator.validate(authnRequest, SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        assertThat(exception.getMessage()).contains("Error");
    }
}
