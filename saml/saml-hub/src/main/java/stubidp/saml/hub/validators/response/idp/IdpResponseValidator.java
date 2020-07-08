package stubidp.saml.hub.validators.response.idp;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.hub.validators.response.idp.components.EncryptedResponseFromIdpValidator;
import stubidp.saml.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.util.List;

public class IdpResponseValidator {
    private final SamlResponseSignatureValidator samlResponseSignatureValidator;
    private final AssertionDecrypter assertionDecrypter;
    private final SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;
    @SuppressWarnings("rawtypes")
    private final EncryptedResponseFromIdpValidator responseFromIdpValidator;
    private final DestinationValidator responseDestinationValidator;
    private final ResponseAssertionsFromIdpValidator responseAssertionsFromIdpValidator;
    private ValidatedResponse validatedResponse;
    private ValidatedAssertions validatedAssertions;

    public IdpResponseValidator(SamlResponseSignatureValidator samlResponseSignatureValidator,
                                AssertionDecrypter assertionDecrypter,
                                SamlAssertionsSignatureValidator samlAssertionsSignatureValidator,
                                @SuppressWarnings("rawtypes")
                                EncryptedResponseFromIdpValidator responseFromIdpValidator,
                                DestinationValidator responseDestinationValidator,
                                ResponseAssertionsFromIdpValidator responseAssertionsFromIdpValidator) {
        this.samlResponseSignatureValidator = samlResponseSignatureValidator;
        this.assertionDecrypter = assertionDecrypter;
        this.samlAssertionsSignatureValidator = samlAssertionsSignatureValidator;
        this.responseFromIdpValidator = responseFromIdpValidator;
        this.responseDestinationValidator = responseDestinationValidator;
        this.responseAssertionsFromIdpValidator = responseAssertionsFromIdpValidator;
    }

    public ValidatedResponse getValidatedResponse() {
        return validatedResponse;
    }

    public ValidatedAssertions getValidatedAssertions() {
        return validatedAssertions;
    }

    public void validate(Response response) {
        responseFromIdpValidator.validate(response);
        responseDestinationValidator.validate(response.getDestination());

        validatedResponse = samlResponseSignatureValidator.validate(response, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(validatedResponse);
        validatedAssertions = samlAssertionsSignatureValidator.validate(decryptedAssertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        responseAssertionsFromIdpValidator.validate(validatedResponse, validatedAssertions);
    }
}
