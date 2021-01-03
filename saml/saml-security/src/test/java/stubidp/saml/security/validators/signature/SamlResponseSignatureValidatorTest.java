package stubidp.saml.security.validators.signature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationResponse;
import stubidp.saml.security.SamlMessageSignatureValidator;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.security.errors.SamlTransformationErrorFactory.unableToDecrypt;

@ExtendWith(MockitoExtension.class)
public class SamlResponseSignatureValidatorTest extends OpenSAMLRunner {

    private static final String RESPONSE_ID = "RESPONSEID";
    private SamlMessageSignatureValidator samlMessageSignatureValidator;
    private SamlResponseSignatureValidator responseSignatureValidator;
    private Response response;

    @BeforeEach
    void setUp() {
        samlMessageSignatureValidator = mock(SamlMessageSignatureValidator.class);
        responseSignatureValidator = new SamlResponseSignatureValidator(samlMessageSignatureValidator);
        response = mock(Response.class);
    }

    @Test
    void validate_shouldDoNothingIfResponseSignatureIsValid() {
        when(samlMessageSignatureValidator.validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(SamlValidationResponse.aValidResponse());
        when(response.getID()).thenReturn(RESPONSE_ID);
        
        ValidatedResponse validatedResponse = responseSignatureValidator.validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        verify(samlMessageSignatureValidator).validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        assertThat(validatedResponse.getID()).isEqualTo(RESPONSE_ID);
    }

    @Test
    void validate_shouldThrowExceptionIfResponseSignatureIsInvalid() {
        SamlValidationResponse invalidResponse = SamlValidationResponse.anInvalidResponse(unableToDecrypt("Error"));
        when(samlMessageSignatureValidator.validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(invalidResponse);

        final SamlTransformationErrorException exception = Assertions.assertThrows(SamlTransformationErrorException.class, () -> responseSignatureValidator.validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        assertThat(exception.getMessage()).contains("Error");
    }
}