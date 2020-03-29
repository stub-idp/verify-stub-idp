package stubidp.saml.security;

import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.extensions.validation.SamlValidationResponse;
import stubidp.saml.security.errors.SamlTransformationErrorFactory;
import stubidp.saml.security.validators.signature.SamlSignatureUtil;

import javax.xml.namespace.QName;
import java.util.Objects;

import static stubidp.saml.security.errors.SamlTransformationErrorFactory.invalidMessageSignature;
import static stubidp.saml.security.errors.SamlTransformationErrorFactory.unableToValidateMessageSignature;

public class SamlMessageSignatureValidator {

    private static final Logger LOG = LoggerFactory.getLogger(SamlMessageSignatureValidator.class);

    private final SignatureValidator signatureValidator;

    public SamlMessageSignatureValidator(SignatureValidator signatureValidator) {
        this.signatureValidator = signatureValidator;
    }

    public SamlValidationResponse validate(Response response, QName role) {
        return validateWithIssuer(response, response.getIssuer(), role);
    }

    public SamlValidationResponse validate(Assertion assertion, QName role) {
        return validateWithIssuer(assertion, assertion.getIssuer(), role);
    }

    /**
     * @param request - an AttributeQuery or AuthnRequest to validate
     * @return a SamlValidationResponse indicating if the signature was valid
     */
    public SamlValidationResponse validate(RequestAbstractType request, QName role) {
        return validateWithIssuer(request, request.getIssuer(), role);
    }

    private SamlValidationResponse validateWithIssuer(SignableSAMLObject request, Issuer issuer, QName role) {
        if (Objects.isNull(issuer)) {
            return SamlValidationResponse.anInvalidResponse(SamlTransformationErrorFactory.missingIssuer());
        }
        String issuerString = issuer.getValue();
        if (Objects.isNull(issuerString) || issuerString.isBlank()) {
            return SamlValidationResponse.anInvalidResponse(SamlTransformationErrorFactory.emptyIssuer());
        }
        return validateWithIssuer(request, issuerString, role);
    }

    private SamlValidationResponse validateWithIssuer(SignableSAMLObject signableSAMLObject, String issuerId, QName role) {
        if (Objects.isNull(signableSAMLObject.getSignature())) {
            return SamlValidationResponse.anInvalidResponse(SamlTransformationErrorFactory.missingSignature());
        }
        if (!SamlSignatureUtil.isSignaturePresent(signableSAMLObject.getSignature())) {
            return SamlValidationResponse.anInvalidResponse(SamlTransformationErrorFactory.signatureNotSigned());
        }
        try {
            if (signatureValidator.validate(signableSAMLObject, issuerId, role)) {
                return SamlValidationResponse.aValidResponse();
            }
            else {
                return SamlValidationResponse.anInvalidResponse(invalidMessageSignature());
            }

        } catch (org.opensaml.security.SecurityException e) {
            LOG.warn("There was an unexpected error validating the message signature using the provided certificate.", e);
            return SamlValidationResponse.anInvalidResponse(unableToValidateMessageSignature(), e);
        } catch (SignatureException e) {
            LOG.error("XML Signature invalid (SAML core section 5.4)", e);
            return SamlValidationResponse.anInvalidResponse(unableToValidateMessageSignature(), e);
        }
    }

}
