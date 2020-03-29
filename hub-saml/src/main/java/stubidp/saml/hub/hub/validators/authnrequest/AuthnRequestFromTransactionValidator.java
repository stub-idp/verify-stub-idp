package stubidp.saml.hub.hub.validators.authnrequest;

import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlTransformationErrorManager;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.core.validators.SamlValidator;
import stubidp.saml.hub.hub.exception.SamlDuplicateRequestIdException;
import stubidp.saml.hub.hub.exception.SamlRequestTooOldException;
import stubidp.saml.security.validators.issuer.IssuerValidator;
import stubidp.saml.security.validators.signature.SamlSignatureUtil;

import java.time.Instant;
import java.util.Objects;

public class AuthnRequestFromTransactionValidator implements SamlValidator<AuthnRequest> {

    private final IssuerValidator issuerValidator;
    private final DuplicateAuthnRequestValidator duplicateAuthnRequestValidator;
    private final AuthnRequestIssueInstantValidator issueInstantValidator;

    public AuthnRequestFromTransactionValidator(
            IssuerValidator issuerValidator,
            DuplicateAuthnRequestValidator duplicateAuthnRequestValidator,
            AuthnRequestIssueInstantValidator issueInstantValidator) {
        this.issuerValidator = issuerValidator;
        this.duplicateAuthnRequestValidator = duplicateAuthnRequestValidator;
        this.issueInstantValidator = issueInstantValidator;
    }

    @Override
    public void validate(AuthnRequest request) {
        issuerValidator.validate(request.getIssuer());
        validateRequestId(request);
        validateIssueInstant(request);
        validateSignaturePresence(request);
        validateVersion(request);
        validateNameIdPolicy(request);
        validateScoping(request);
        validateProtocolBinding(request);
        validatePassiveXSBoolean(request);
    }

    private void validateScoping(final AuthnRequest request) {
        if (request.getScoping() != null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.scopingNotAllowed();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    private void validatePassiveXSBoolean(final AuthnRequest request) {
        if (request.isPassiveXSBoolean() != null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.isPassiveNotAllowed();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    private void validateRequestId(final AuthnRequest request) {
        final String requestId = request.getID();
        if (Objects.isNull(requestId) || requestId.isBlank()) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingRequestId();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        if (!requestIdStartsWithUnderscoreOrLetter(requestId)) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.invalidRequestID();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        if (!duplicateAuthnRequestValidator.valid(request.getID())) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.duplicateRequestId(request.getID(), request.getIssuer().getValue());
            throw new SamlDuplicateRequestIdException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    private boolean requestIdStartsWithUnderscoreOrLetter(final String requestId) {
        String firstCharacter = requestId.substring(0, 1);
        return firstCharacter.equals("_") || firstCharacter.matches("[a-zA-Z]");
    }

    private void validateSignaturePresence(final AuthnRequest request) {
        Signature signature = request.getSignature();
        if (signature == null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingSignature();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
        if (!SamlSignatureUtil.isSignaturePresent(signature)) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.signatureNotSigned();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    private void validateVersion(final AuthnRequest request) {
        final String requestId = request.getID();
        if (request.getVersion() == null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingRequestVersion(requestId);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        if (request.getVersion() != SAMLVersion.VERSION_20) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.illegalRequestVersionNumber();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    private void validateIssueInstant(final AuthnRequest request) {
        final String requestId = request.getID();
        Instant issueInstant = request.getIssueInstant();
        if (issueInstant == null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingRequestIssueInstant(requestId);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        if (!issueInstantValidator.isValid(issueInstant)) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.requestTooOld(request.getID(), issueInstant, Instant.now());
            throw new SamlRequestTooOldException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }

    private void validateNameIdPolicy(AuthnRequest request) {
        NameIDPolicy nameIDPolicy = request.getNameIDPolicy();
        if (nameIDPolicy != null) {
            if (nameIDPolicy.getFormat() == null) {
                SamlTransformationErrorManager.warn(SamlTransformationErrorFactory.missingNameIDPolicy());
            } else if (!nameIDPolicy.getFormat().equals(NameIDType.PERSISTENT)) {
                SamlTransformationErrorManager.warn(SamlTransformationErrorFactory.illegalNameIDPolicy(nameIDPolicy.getFormat()));
            }
        }
    }

    private void validateProtocolBinding(final AuthnRequest request) {
        String protocolBinding = request.getProtocolBinding();
        if (protocolBinding != null) {
            if (!protocolBinding.equals(SAMLConstants.SAML2_POST_BINDING_URI)) {
                SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.illegalProtocolBindingError(protocolBinding, SAMLConstants.SAML2_POST_BINDING_URI);
                throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
            }
        }
    }

}
