package stubidp.saml.security.validators.signature;

import org.opensaml.saml.saml2.core.RequestAbstractType;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationResponse;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.security.SamlMessageSignatureValidator;

import javax.xml.namespace.QName;

public class SamlRequestSignatureValidator<T extends RequestAbstractType> {

    private final SamlMessageSignatureValidator samlMessageSignatureValidator;


    public SamlRequestSignatureValidator(SamlMessageSignatureValidator samlMessageSignatureValidator) {
        this.samlMessageSignatureValidator = samlMessageSignatureValidator;
    }

    public void validate(T samlMessage, QName role) {
        SamlValidationResponse samlValidationResponse = samlMessageSignatureValidator.validate(samlMessage, role);

        if (samlValidationResponse.isOK()) return;

        SamlValidationSpecificationFailure failure = samlValidationResponse.getSamlValidationSpecificationFailure();
        throw new SamlTransformationErrorException(failure.getErrorMessage(), samlValidationResponse.getCause(), failure.getLogLevel());
    }
}
