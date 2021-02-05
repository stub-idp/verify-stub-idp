package stubidp.saml.metadata;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.security.MetadataBackedSignatureValidator;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.SamlMessageSignatureValidator;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;

import javax.inject.Inject;
import java.util.List;

import static java.text.MessageFormat.format;

public class EidasValidatorFactory {

    private MetadataResolverRepository eidasMetadataResolverRepository;

    @Inject
    public EidasValidatorFactory(MetadataResolverRepository eidasMetadataResolverRepository) {
        this.eidasMetadataResolverRepository = eidasMetadataResolverRepository;
    }

    public ValidatedResponse getValidatedResponse(Response response) {
        SamlResponseSignatureValidator samlResponseSignatureValidator = new SamlResponseSignatureValidator(getSamlMessageSignatureValidator(response.getIssuer().getValue()));
        return samlResponseSignatureValidator.validate(response, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    public void getValidatedAssertion(ValidatedResponse validatedResponse, List<Assertion> decryptedAssertions) {
        SamlAssertionsSignatureValidator samlAssertionsSignatureValidator = new SamlAssertionsSignatureValidator(getSamlMessageSignatureValidator(validatedResponse.getIssuer().getValue()));
        samlAssertionsSignatureValidator.validateEidas(decryptedAssertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    private SamlMessageSignatureValidator getSamlMessageSignatureValidator(String entityId) {
        return eidasMetadataResolverRepository
                .getSignatureTrustEngine(entityId)
                .map(MetadataBackedSignatureValidator::withoutCertificateChainValidation)
                .map(SamlMessageSignatureValidator::new)
                .orElseThrow(() -> new SamlTransformationErrorException(format("Unable to find metadata resolver for entity Id {0}", entityId), Level.ERROR));
    }

}
