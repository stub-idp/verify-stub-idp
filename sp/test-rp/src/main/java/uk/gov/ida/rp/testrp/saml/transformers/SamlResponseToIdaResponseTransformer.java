package uk.gov.ida.rp.testrp.saml.transformers;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.util.List;
import java.util.function.Function;

public class SamlResponseToIdaResponseTransformer implements Function<Response, InboundResponseFromHub> {

    private final InboundResponseFromHubUnmarshaller inboundResponseFromHubUnmarshaller;
    private final SamlResponseSignatureValidator samlResponseSignatureValidator;
    private final AssertionDecrypter assertionDecrypter;
    private final SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;
    private final boolean isHubExpectedToSignAuthnResponse;

    public SamlResponseToIdaResponseTransformer(
            InboundResponseFromHubUnmarshaller inboundResponseFromHubUnmarshaller,
            SamlResponseSignatureValidator samlResponseSignatureValidator,
            AssertionDecrypter assertionDecrypter,
            SamlAssertionsSignatureValidator samlAssertionsSignatureValidator,
            boolean isHubExpectedToSignAuthnResponse) {
        this.inboundResponseFromHubUnmarshaller = inboundResponseFromHubUnmarshaller;
        this.samlResponseSignatureValidator = samlResponseSignatureValidator;
        this.assertionDecrypter = assertionDecrypter;
        this.samlAssertionsSignatureValidator = samlAssertionsSignatureValidator;
        this.isHubExpectedToSignAuthnResponse = isHubExpectedToSignAuthnResponse;
    }

    @Override
    public InboundResponseFromHub apply(final Response response) {
        ValidatedResponse validatedResponse;
        if(isHubExpectedToSignAuthnResponse) {
            validatedResponse = samlResponseSignatureValidator.validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        } else {
            validatedResponse = new ValidatedResponse(response);
        }
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(validatedResponse);
        ValidatedAssertions validatedAssertions = samlAssertionsSignatureValidator.validate(decryptedAssertions, AttributeAuthorityDescriptor.DEFAULT_ELEMENT_NAME);

        return inboundResponseFromHubUnmarshaller.fromSaml(validatedResponse, validatedAssertions);
    }

}
