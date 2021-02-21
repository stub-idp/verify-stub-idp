package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import stubidp.saml.metadata.EidasMetadataResolverRepository;
import stubidp.saml.utils.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import stubidp.saml.utils.core.transformers.MatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import stubidp.saml.utils.hub.factories.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.validators.EidasAssertionTranslatorValidatorContainer;

import java.util.List;

import static java.util.Collections.singletonList;

public class EidasAssertionTranslator extends BaseEidasAssertionTranslator {
    public EidasAssertionTranslator(
            EidasAssertionTranslatorValidatorContainer validatorContainer,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
            EidasMetadataResolverRepository metadataResolverRepository,
            SignatureValidatorFactory signatureValidatorFactory,
            List<String> acceptableHubConnectorEntityIds,
            UserIdHashFactory userIdHashFactory) {
        super(
                validatorContainer,
                matchingDatasetUnmarshaller,
                mdsMapper,
                metadataResolverRepository,
                signatureValidatorFactory,
                acceptableHubConnectorEntityIds,
                userIdHashFactory
        );
    }

    @Override
    protected void validateSignature(Assertion assertion, String issuerEntityId) {
        metadataResolverRepository.getSignatureTrustEngine(issuerEntityId)
                .map(signatureValidatorFactory::getSignatureValidator)
                .orElseThrow(() -> new SamlResponseValidationException("Unable to find metadata resolver for entity Id " + issuerEntityId))
                .validate(singletonList(assertion), IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }
}
