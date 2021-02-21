package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.metadata.EidasMetadataResolverRepository;
import stubidp.saml.utils.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import stubidp.saml.utils.core.transformers.MatchingDatasetUnmarshaller;
import stubidp.saml.utils.hub.factories.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.validators.EidasAssertionTranslatorValidatorContainer;

import java.util.List;

public class EidasUnsignedAssertionTranslator extends BaseEidasAssertionTranslator {
    public EidasUnsignedAssertionTranslator(
            EidasAssertionTranslatorValidatorContainer validatorContainer,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
            EidasMetadataResolverRepository metadataResolverRepository,
            List<String> acceptableHubConnectorEntityIds,
            UserIdHashFactory userIdHashFactory) {
        super(
                validatorContainer,
                matchingDatasetUnmarshaller,
                mdsMapper,
                metadataResolverRepository,
                null,
                acceptableHubConnectorEntityIds,
                userIdHashFactory
        );
    }

    @Override
    protected void validateSignature(Assertion assertion, String issuerEntityId) {
        // No need to validate the signature of an unsigned assertion.
    }
}
