package uk.gov.ida.matchingserviceadapter.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationResponse;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.matchingserviceadapter.validators.AttributeQuerySignatureValidator;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AttributeQuerySignatureValidatorTest {

    private AttributeQuerySignatureValidator validator;
    private SamlMessageSignatureValidator samlMessageSignatureValidator;

    @BeforeEach
    public void setUp() {
        samlMessageSignatureValidator = mock(SamlMessageSignatureValidator.class);
    }

    @Test
    public void shouldNotComplainWhenCorrectDataIsPassed() {
        AttributeQuery attributeQuery = mock(AttributeQuery.class);
        SamlValidationResponse samlValidationResponse = SamlValidationResponse.aValidResponse();
        when(samlMessageSignatureValidator.validate(attributeQuery, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(samlValidationResponse);
        validator = new AttributeQuerySignatureValidator(samlMessageSignatureValidator);

        validator.validate(attributeQuery);
    }

    @Test
    public void shouldThrowExceptionWhenInvalidDataPassed(){
        AttributeQuery attributeQuery = mock(AttributeQuery.class);
        SamlValidationSpecificationFailure samlValidationSpecificationFailure = mock(SamlValidationSpecificationFailure.class);
        SamlValidationResponse samlValidationResponse = SamlValidationResponse.anInvalidResponse(samlValidationSpecificationFailure);
        when(samlMessageSignatureValidator.validate(attributeQuery, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(samlValidationResponse);

        validator = new AttributeQuerySignatureValidator(samlMessageSignatureValidator);

        assertThatExceptionOfType(SamlTransformationErrorException.class)
                .isThrownBy(() -> validator.validate(attributeQuery));
    }

}
