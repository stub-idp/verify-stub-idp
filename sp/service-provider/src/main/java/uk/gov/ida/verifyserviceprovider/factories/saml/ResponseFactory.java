package uk.gov.ida.verifyserviceprovider.factories.saml;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import stubidp.saml.hub.core.validators.assertion.AssertionAttributeStatementValidator;
import stubidp.saml.metadata.EidasMetadataResolverRepository;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.DecrypterFactory;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.MetadataBackedSignatureValidator;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.SamlMessageSignatureValidator;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;
import stubidp.saml.serializers.deserializers.OpenSamlXMLObjectUnmarshaller;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.serializers.deserializers.parser.SamlObjectParser;
import stubidp.saml.serializers.deserializers.validators.Base64StringDecoder;
import stubidp.saml.serializers.deserializers.validators.NotNullSamlStringValidator;
import stubidp.saml.utils.core.transformers.EidasMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import stubidp.saml.utils.core.transformers.VerifyMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.validation.conditions.AudienceRestrictionValidator;
import stubidp.saml.utils.hub.factories.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.configuration.EuropeanIdentityConfiguration;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier;
import uk.gov.ida.verifyserviceprovider.services.AssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EidasAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EidasUnsignedAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.IdentityResponderCodeTranslator;
import uk.gov.ida.verifyserviceprovider.services.MatchingAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.MatchingResponderCodeTranslator;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;
import uk.gov.ida.verifyserviceprovider.services.UnsignedAssertionsResponseHandler;
import uk.gov.ida.verifyserviceprovider.services.VerifyAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.EidasAssertionTranslatorValidatorContainer;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.ResponseSizeValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;
import uk.gov.ida.verifyserviceprovider.validators.TimeRestrictionValidator;

import java.security.KeyPair;
import java.util.List;

public class ResponseFactory {

    private static final NotNullSamlStringValidator notNullSamlStringValidator = new NotNullSamlStringValidator();
    private static final Base64StringDecoder base64StringDecoder = new Base64StringDecoder();
    private static final ResponseSizeValidator responseSizeValidator = new ResponseSizeValidator();
    private static final SamlObjectParser samlObjectParser = new SamlObjectParser();
    private static final OpenSamlXMLObjectUnmarshaller<Response> responseOpenSamlXMLObjectUnmarshaller = new OpenSamlXMLObjectUnmarshaller<>(samlObjectParser);
    private static final EncryptionAlgorithmValidator encryptionAlgorithmValidator = new EncryptionAlgorithmValidator();
    private static final DecrypterFactory decrypterFactory = new DecrypterFactory();

    private final List<KeyPair> encryptionKeyPairs;
    private final IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever;

    public ResponseFactory(List<KeyPair> encryptionKeyPairs) {
        this.encryptionKeyPairs = encryptionKeyPairs;
        this.idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(createEncryptionKeyStore());
    }

    public static StringToOpenSamlObjectTransformer<Response> createStringToResponseTransformer() {
        return new StringToOpenSamlObjectTransformer<>(
                notNullSamlStringValidator,
                base64StringDecoder,
                responseSizeValidator,
                responseOpenSamlXMLObjectUnmarshaller
        );
    }

    public ResponseService createMatchingResponseService(
            ExplicitKeySignatureTrustEngine hubSignatureTrustEngine,
            AssertionTranslator matchingAssertionTranslator,
            DateTimeComparator dateTimeComparator) {

        final AssertionDecrypter assertionDecrypter = createAssertionDecrypter();
        final MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator(hubSignatureTrustEngine);

        return new ResponseService(
                createStringToResponseTransformer(),
                assertionDecrypter,
                matchingAssertionTranslator,
                new SamlResponseSignatureValidator(new SamlMessageSignatureValidator(metadataBackedSignatureValidator)),
                new InstantValidator(dateTimeComparator),
                new MatchingResponderCodeTranslator(),
                null
        );
    }

    public ResponseService createNonMatchingResponseService(
            ExplicitKeySignatureTrustEngine hubSignatureTrustEngine,
            AssertionTranslator nonMatchingAssertionTranslator,
            DateTimeComparator dateTimeComparator,
            UnsignedAssertionsResponseHandler unsignedAssertionsResponseHandler) {
        final AssertionDecrypter assertionDecrypter = createAssertionDecrypter();
        final MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator(hubSignatureTrustEngine);
        final StringToOpenSamlObjectTransformer<Response> stringToResponseTransformer = createStringToResponseTransformer();

        return new ResponseService(
                stringToResponseTransformer,
                assertionDecrypter,
                nonMatchingAssertionTranslator,
                new SamlResponseSignatureValidator(new SamlMessageSignatureValidator(metadataBackedSignatureValidator)),
                new InstantValidator(dateTimeComparator),
                new IdentityResponderCodeTranslator(),
                unsignedAssertionsResponseHandler
        );
    }

    public MatchingAssertionTranslator createMsaAssertionTranslator(
            ExplicitKeySignatureTrustEngine signatureTrustEngine,
            SignatureValidatorFactory signatureValidatorFactory,
            DateTimeComparator dateTimeComparator) {
        final TimeRestrictionValidator timeRestrictionValidator = new TimeRestrictionValidator(dateTimeComparator);
        final SamlAssertionsSignatureValidator signatureValidator = signatureValidatorFactory.getSignatureValidator(signatureTrustEngine);
        final AssertionValidator assertionValidator = new AssertionValidator(
                new InstantValidator(dateTimeComparator),
                new SubjectValidator(timeRestrictionValidator),
                new ConditionsValidator(timeRestrictionValidator, new AudienceRestrictionValidator()));

        return new MatchingAssertionTranslator(
                assertionValidator,
                new LevelOfAssuranceValidator(),
                signatureValidator);
    }

    public VerifyAssertionTranslator createVerifyIdpAssertionTranslator(
            SamlAssertionsSignatureValidator hubSignatureValidator,
            DateTimeComparator dateTimeComparator,
            String hashingEntityId) {
        final TimeRestrictionValidator timeRestrictionValidator = new TimeRestrictionValidator(dateTimeComparator);

        return new VerifyAssertionTranslator(
                hubSignatureValidator,
                new SubjectValidator(timeRestrictionValidator),
                new AssertionAttributeStatementValidator(),
                new VerifyMatchingDatasetUnmarshaller(),
                new AssertionClassifier(),
                new MatchingDatasetToNonMatchingAttributesMapper(),
                new LevelOfAssuranceValidator(),
                new UserIdHashFactory(hashingEntityId));
    }

    public EidasAssertionTranslator createEidasAssertionTranslator(
            DateTimeComparator dateTimeComparator,
            EidasMetadataResolverRepository eidasMetadataResolverRepository,
            EuropeanIdentityConfiguration europeanIdentityConfiguration,
            String hashingEntityId) {
        return new EidasAssertionTranslator(
                getEidasAssertionValidatorContainer(dateTimeComparator),
                new EidasMatchingDatasetUnmarshaller(),
                new MatchingDatasetToNonMatchingAttributesMapper(),
                eidasMetadataResolverRepository,
                new SignatureValidatorFactory(),
                europeanIdentityConfiguration.getAllAcceptableHubConnectorEntityIds(),
                new UserIdHashFactory(hashingEntityId));
    }

    public EidasUnsignedAssertionTranslator createEidasUnsignedAssertionTranslator(
            DateTimeComparator dateTimeComparator,
            EidasMetadataResolverRepository eidasMetadataResolverRepository,
            EuropeanIdentityConfiguration europeanIdentityConfiguration,
            String hashingEntityId) {
        return new EidasUnsignedAssertionTranslator(
                getEidasAssertionValidatorContainer(dateTimeComparator),
                new EidasMatchingDatasetUnmarshaller(),
                new MatchingDatasetToNonMatchingAttributesMapper(),
                eidasMetadataResolverRepository,
                europeanIdentityConfiguration.getAllAcceptableHubConnectorEntityIds(),
                new UserIdHashFactory(hashingEntityId));
    }

    private AssertionDecrypter createAssertionDecrypter() {
        final List<Credential> decryptingCredentials = idaKeyStoreCredentialRetriever.getDecryptingCredentials();
        return new AssertionDecrypter(
                encryptionAlgorithmValidator,
                decrypterFactory.createDecrypter(decryptingCredentials));
    }

    private MetadataBackedSignatureValidator createMetadataBackedSignatureValidator(ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine) {
        return MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
    }

    private IdaKeyStore createEncryptionKeyStore() {
        return new IdaKeyStore(null, encryptionKeyPairs);
    }

    private EidasAssertionTranslatorValidatorContainer getEidasAssertionValidatorContainer(DateTimeComparator dateTimeComparator) {
        final TimeRestrictionValidator timeRestrictionValidator = new TimeRestrictionValidator(dateTimeComparator);
        return new EidasAssertionTranslatorValidatorContainer(
                new SubjectValidator(timeRestrictionValidator),
                new InstantValidator(dateTimeComparator),
                new ConditionsValidator(timeRestrictionValidator, new AudienceRestrictionValidator()),
                new LevelOfAssuranceValidator());
    }
}
