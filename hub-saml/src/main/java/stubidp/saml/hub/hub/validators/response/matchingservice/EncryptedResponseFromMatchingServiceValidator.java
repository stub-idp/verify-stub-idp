package stubidp.saml.hub.hub.validators.response.matchingservice;

import com.google.common.base.Strings;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.hub.exception.SamlValidationException;
import stubidp.saml.hub.hub.validators.response.common.IssuerValidator;
import stubidp.saml.hub.hub.validators.response.common.RequestIdValidator;

import java.util.List;

import static stubidp.saml.security.validators.signature.SamlSignatureUtil.isSignaturePresent;

public class EncryptedResponseFromMatchingServiceValidator {

    public void validate(Response response) {
        IssuerValidator.validate(response);
        RequestIdValidator.validate(response);
        validateResponse(response);
    }

    private void validateResponse(Response response) {
        if (Strings.isNullOrEmpty(response.getID())) throw new SamlValidationException(SamlTransformationErrorFactory.missingId());

        Signature signature = response.getSignature();
        if (signature == null) throw new SamlValidationException(SamlTransformationErrorFactory.missingSignature());
        if (!isSignaturePresent(signature)) throw new SamlValidationException(SamlTransformationErrorFactory.signatureNotSigned());

        validateStatusAndSubStatus(response);
        validateAssertionPresence(response);
    }

    protected void validateStatusAndSubStatus(Response response) {
        StatusCode statusCode = response.getStatus().getStatusCode();
        String statusCodeValue = statusCode.getValue();

        StatusCode subStatusCode = statusCode.getStatusCode();

        if (StatusCode.REQUESTER.equals(statusCodeValue)) return;

        if (subStatusCode == null) throw new SamlValidationException(SamlTransformationErrorFactory.missingSubStatus());

        String subStatusCodeValue = subStatusCode.getValue();

        if (!StatusCode.RESPONDER.equals(statusCodeValue)) {
            validateSuccessResponse(statusCodeValue, subStatusCodeValue);
        } else {
            validateResponderError(subStatusCodeValue);
        }
    }

    private void validateResponderError(String subStatusCodeValue) {
        if (List.of(
            SamlStatusCode.NO_MATCH,
            SamlStatusCode.MULTI_MATCH,
            SamlStatusCode.CREATE_FAILURE).contains(subStatusCodeValue)) {
            return;
        }

        throw new SamlValidationException(SamlTransformationErrorFactory.subStatusMustBeOneOf("Responder", "No Match", "Multi Match", "Create Failure"));
    }

    private void validateSuccessResponse(String statusCodeValue, String subStatusCodeValue) {
        if (!StatusCode.SUCCESS.equals(statusCodeValue)) return;
        if (List.of(
                SamlStatusCode.MATCH,
                SamlStatusCode.NO_MATCH,
                SamlStatusCode.CREATED).contains(subStatusCodeValue)) {
            return;
        }

        throw new SamlValidationException(SamlTransformationErrorFactory.subStatusMustBeOneOf("Success", "Match", "No Match", "Created"));
    }

    protected void validateAssertionPresence(Response response) {
        if (!response.getAssertions().isEmpty()) throw new SamlValidationException(SamlTransformationErrorFactory.unencryptedAssertion());

        boolean responseWasSuccessful = StatusCode.SUCCESS.equals(response.getStatus().getStatusCode().getValue());
        boolean responseHasNoAssertions = response.getEncryptedAssertions().isEmpty();

        if (responseWasSuccessful && responseHasNoAssertions)
            throw new SamlValidationException(SamlTransformationErrorFactory.missingSuccessUnEncryptedAssertions());

        if (!responseWasSuccessful && !responseHasNoAssertions) {
            throw new SamlValidationException(SamlTransformationErrorFactory.nonSuccessHasUnEncryptedAssertions());
        }

        if (response.getEncryptedAssertions().size() > 1) {
            throw new SamlValidationException(SamlTransformationErrorFactory.unexpectedNumberOfAssertions(1, response.getEncryptedAssertions().size()));
        }
    }
}
