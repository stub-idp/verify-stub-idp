package stubidp.saml.hub.api;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.w3c.dom.Element;
import stubidp.saml.domain.matching.HubEidasAttributeQueryRequest;
import stubidp.saml.domain.matching.MatchingServiceHealthCheckRequest;
import stubidp.saml.domain.request.EidasAuthnRequestFromHub;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.saml.domain.response.InboundResponseFromIdp;
import stubidp.saml.domain.response.OutboundResponseFromHub;
import stubidp.saml.hub.core.transformers.outbound.decorators.SamlAttributeQueryAssertionEncrypter;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.hub.core.validators.assertion.AssertionAttributeStatementValidator;
import stubidp.saml.hub.core.validators.assertion.AssertionValidator;
import stubidp.saml.hub.core.validators.assertion.AuthnStatementAssertionValidator;
import stubidp.saml.hub.core.validators.assertion.DuplicateAssertionValidatorImpl;
import stubidp.saml.hub.core.validators.assertion.IPAddressValidator;
import stubidp.saml.hub.core.validators.assertion.IdentityProviderAssertionValidator;
import stubidp.saml.hub.core.validators.assertion.MatchingDatasetAssertionValidator;
import stubidp.saml.hub.core.validators.subject.AssertionSubjectValidator;
import stubidp.saml.hub.core.validators.subjectconfirmation.AssertionSubjectConfirmationValidator;
import stubidp.saml.hub.core.validators.subjectconfirmation.BasicAssertionSubjectConfirmationValidator;
import stubidp.saml.hub.configuration.SamlAuthnRequestValidityDurationConfiguration;
import stubidp.saml.hub.configuration.SamlDuplicateRequestValidationConfiguration;
import stubidp.saml.hub.domain.AuthnRequestFromRelyingParty;
import stubidp.saml.hub.domain.Endpoints;
import stubidp.saml.domain.matching.HubAttributeQueryRequest;
import stubidp.saml.hub.domain.InboundResponseFromMatchingService;
import stubidp.saml.hub.factories.AttributeQueryAttributeFactory;
import stubidp.saml.hub.transformers.inbound.AuthnRequestFromRelyingPartyUnmarshaller;
import stubidp.saml.hub.transformers.inbound.AuthnRequestToIdaRequestFromRelyingPartyTransformer;
import stubidp.saml.hub.transformers.inbound.IdaResponseFromIdpUnmarshaller;
import stubidp.saml.hub.transformers.inbound.IdpIdaStatusUnmarshaller;
import stubidp.saml.hub.transformers.inbound.InboundHealthCheckResponseFromMatchingServiceUnmarshaller;
import stubidp.saml.hub.transformers.inbound.InboundResponseFromMatchingServiceUnmarshaller;
import stubidp.saml.hub.transformers.inbound.MatchingServiceIdaStatusUnmarshaller;
import stubidp.saml.hub.transformers.inbound.PassthroughAssertionUnmarshaller;
import stubidp.saml.hub.transformers.inbound.SamlStatusToIdaStatusCodeMapper;
import stubidp.saml.hub.transformers.inbound.providers.DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer;
import stubidp.saml.hub.transformers.inbound.providers.DecoratedSamlResponseToInboundHealthCheckResponseFromMatchingServiceTransformer;
import stubidp.saml.hub.transformers.inbound.providers.DecoratedSamlResponseToInboundResponseFromMatchingServiceTransformer;
import stubidp.saml.hub.transformers.outbound.AssertionFromIdpToAssertionTransformer;
import stubidp.saml.hub.transformers.outbound.AttributeQueryToElementTransformer;
import stubidp.saml.hub.transformers.outbound.EidasAuthnRequestFromHubToAuthnRequestTransformer;
import stubidp.saml.hub.transformers.outbound.EncryptedAssertionUnmarshaller;
import stubidp.saml.hub.transformers.outbound.HubAssertionMarshaller;
import stubidp.saml.hub.transformers.outbound.HubAttributeQueryRequestToSamlAttributeQueryTransformer;
import stubidp.saml.hub.transformers.outbound.HubEidasAttributeQueryRequestToSamlAttributeQueryTransformer;
import stubidp.saml.hub.transformers.outbound.IdaAuthnRequestFromHubToAuthnRequestTransformer;
import stubidp.saml.hub.transformers.outbound.MatchingServiceHealthCheckRequestToSamlAttributeQueryTransformer;
import stubidp.saml.hub.transformers.outbound.OutboundResponseFromHubToSamlResponseTransformer;
import stubidp.saml.hub.transformers.outbound.RequestAbstractTypeToStringTransformer;
import stubidp.saml.hub.transformers.outbound.SamlProfileTransactionIdaStatusMarshaller;
import stubidp.saml.hub.transformers.outbound.TransactionIdaStatusMarshaller;
import stubidp.saml.hub.transformers.outbound.decorators.NoOpSamlAttributeQueryAssertionEncrypter;
import stubidp.saml.hub.transformers.outbound.decorators.SamlAttributeQueryAssertionSignatureSigner;
import stubidp.saml.hub.transformers.outbound.decorators.SigningRequestAbstractTypeSignatureCreator;
import stubidp.saml.hub.validators.authnrequest.AuthnRequestFromTransactionValidator;
import stubidp.saml.hub.validators.authnrequest.AuthnRequestIdKey;
import stubidp.saml.hub.validators.authnrequest.AuthnRequestIssueInstantValidator;
import stubidp.saml.hub.validators.authnrequest.DuplicateAuthnRequestValidator;
import stubidp.saml.hub.validators.authnrequest.IdExpirationCache;
import stubidp.saml.hub.validators.response.common.AssertionSizeValidator;
import stubidp.saml.hub.validators.response.common.ResponseSizeValidator;
import stubidp.saml.hub.validators.response.idp.IdpResponseValidator;
import stubidp.saml.hub.validators.response.idp.components.EncryptedResponseFromIdpValidator;
import stubidp.saml.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import stubidp.saml.hub.validators.response.matchingservice.EncryptedResponseFromMatchingServiceValidator;
import stubidp.saml.hub.validators.response.matchingservice.HealthCheckResponseFromMatchingServiceValidator;
import stubidp.saml.hub.validators.response.matchingservice.ResponseAssertionsFromMatchingServiceValidator;
import stubidp.saml.hub.metadata.domain.HubIdentityProviderMetadataDto;
import stubidp.saml.hub.metadata.transformers.HubIdentityProviderMetadataDtoToEntityDescriptorTransformer;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.DecrypterFactory;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.KeyStoreBackedEncryptionCredentialResolver;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.SamlMessageSignatureValidator;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.security.SignatureValidator;
import stubidp.saml.security.SigningCredentialFactory;
import stubidp.saml.security.SigningKeyStore;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import stubidp.saml.security.validators.issuer.IssuerValidator;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.core.transformers.AuthnContextFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.core.transformers.outbound.decorators.ResponseAssertionSigner;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlSignatureSigner;
import stubidp.saml.utils.hub.factories.AttributeFactory_1_1;
import stubidp.saml.utils.hub.transformers.inbound.decorators.AuthnRequestSizeValidator;
import stubidp.saml.utils.hub.validators.StringSizeValidator;
import stubidp.utils.security.security.IdGenerator;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class HubTransformersFactory {

    private final CoreTransformersFactory coreTransformersFactory;

    public HubTransformersFactory() {
        coreTransformersFactory = new CoreTransformersFactory();
    }

    public Function<OutboundResponseFromHub, String> getOutboundResponseFromHubToStringTransformer(
            final EncryptionKeyStore encryptionKeyStore,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final SignatureAlgorithm signatureAlgorithm,
            final DigestAlgorithm digestAlgorithm) {
        Function<OutboundResponseFromHub, Response> outboundToResponseTransformer = getOutboundResponseFromHubToSamlResponseTransformer();
        Function<Response, String> responseStringTransformer = coreTransformersFactory.getResponseStringTransformer(
                encryptionKeyStore,
                keyStore,
                entityToEncryptForLocator,
                signatureAlgorithm,
                digestAlgorithm);

        return responseStringTransformer.compose(outboundToResponseTransformer);
    }

    public Function<OutboundResponseFromHub, String> getOutboundResponseFromHubToStringTransformer(
            final EncryptionKeyStore encryptionKeyStore,
            final IdaKeyStore keystore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final ResponseAssertionSigner responseAssertionSigner,
            final SignatureAlgorithm signatureAlgorithm,
            final DigestAlgorithm digestAlgorithm) {
        Function<OutboundResponseFromHub, Response> outboundToResponseTransformer = getOutboundResponseFromHubToSamlResponseTransformer();
        Function<Response, String> responseStringTransformer = coreTransformersFactory.getResponseStringTransformer(
                encryptionKeyStore,
                keystore,
                entityToEncryptForLocator,
                responseAssertionSigner,
                signatureAlgorithm,
                digestAlgorithm
        );

        return responseStringTransformer.compose(outboundToResponseTransformer);
    }

    public Function<OutboundResponseFromHub, String> getSamlProfileOutboundResponseFromHubToStringTransformer(
            final EncryptionKeyStore encryptionKeyStore,
            final IdaKeyStore keystore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final ResponseAssertionSigner responseAssertionSigner,
            final SignatureAlgorithm signatureAlgorithm,
            final DigestAlgorithm digestAlgorithm) {
        Function<OutboundResponseFromHub, Response> outboundToResponseTransformer = getSamlProfileOutboundResponseFromHubToSamlResponseTransformer();
        Function<Response, String> responseStringTransformer = coreTransformersFactory.getResponseStringTransformer(
                encryptionKeyStore,
                keystore,
                entityToEncryptForLocator,
                responseAssertionSigner,
                signatureAlgorithm,
                digestAlgorithm
        );

        return responseStringTransformer.compose(outboundToResponseTransformer);
    }

    public Function<HubIdentityProviderMetadataDto, Element> getHubIdentityProviderMetadataDtoToElementTransformer() {
        return
                coreTransformersFactory.<EntityDescriptor>getXmlObjectToElementTransformer().compose(getHubIdentityProviderMetadataDtoToEntityDescriptorTransformer());
    }

    public Function<IdaAuthnRequestFromHub, String> getIdaAuthnRequestFromHubToStringTransformer(IdaKeyStore keyStore, SignatureAlgorithm signatureAlgorithm, DigestAlgorithm digestAlgorithm) {
        return getAuthnRequestToStringTransformer(false, keyStore, signatureAlgorithm, digestAlgorithm).compose(getIdaAuthnRequestFromHubToAuthnRequestTransformer());
    }

    public Function<EidasAuthnRequestFromHub, String> getEidasAuthnRequestFromHubToStringTransformer(IdaKeyStore keyStore, SignatureAlgorithm signatureAlgorithm, DigestAlgorithm digestAlgorithm) {
        return getAuthnRequestToStringTransformer(true, keyStore, signatureAlgorithm, digestAlgorithm).compose(getEidasAuthnRequestFromHubToAuthnRequestTransformer());
    }

    public Function<String, AuthnRequestFromRelyingParty> getStringToIdaAuthnRequestTransformer(
            URI expectedDestinationHost,
            SigningKeyStore signingKeyStore,
            IdaKeyStore decryptionKeyStore,
            IdExpirationCache<AuthnRequestIdKey> duplicateIds,
            SamlDuplicateRequestValidationConfiguration samlDuplicateRequestValidationConfiguration,
            SamlAuthnRequestValidityDurationConfiguration samlAuthnRequestValidityDurationConfiguration
    ) {
        Function<String, AuthnRequest> stringToAuthnRequestTransformer = getStringToAuthnRequestTransformer();
        Function<AuthnRequest, AuthnRequestFromRelyingParty> authnRequestToIdaRequestFromTransactionTransformer =
            getAuthnRequestToAuthnRequestFromTransactionTransformer(
                expectedDestinationHost,
                signingKeyStore,
                decryptionKeyStore,
                duplicateIds,
                samlDuplicateRequestValidationConfiguration,
                samlAuthnRequestValidityDurationConfiguration
            );

        return authnRequestToIdaRequestFromTransactionTransformer.compose(stringToAuthnRequestTransformer);
    }

    public StringToOpenSamlObjectTransformer<AuthnRequest> getStringToAuthnRequestTransformer() {
        return coreTransformersFactory.getStringtoOpenSamlObjectTransformer(
                new AuthnRequestSizeValidator(new StringSizeValidator())
        );
    }

    public StringToOpenSamlObjectTransformer<Response> getStringToResponseTransformer() {
        return coreTransformersFactory.getStringtoOpenSamlObjectTransformer(
                new ResponseSizeValidator(new StringSizeValidator())
        );
    }

    public StringToOpenSamlObjectTransformer<Response> getStringToResponseTransformer(ResponseSizeValidator validator) {
        return coreTransformersFactory.getStringtoOpenSamlObjectTransformer(
                validator
        );
    }

    public StringToOpenSamlObjectTransformer<Assertion> getStringToAssertionTransformer() {
        return coreTransformersFactory.getStringtoOpenSamlObjectTransformer(
                new AssertionSizeValidator()
        );
    }

    public PassthroughAssertionUnmarshaller getAssertionToPassthroughAssertionTransformer() {
        return new PassthroughAssertionUnmarshaller(
                new XmlObjectToBase64EncodedStringTransformer<>(),
                new AuthnContextFactory()
        );
    }

    public AssertionFromIdpToAssertionTransformer getAssertionFromIdpToAssertionTransformer() {
        return new AssertionFromIdpToAssertionTransformer(getStringToAssertionTransformer());
    }

    public Function<HubAttributeQueryRequest, Element> getMatchingServiceRequestToElementTransformer(
            IdaKeyStore keyStore,
            EncryptionKeyStore encryptionKeyStore,
            EntityToEncryptForLocator entity,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm, String hubEntityId) {
        Function<HubAttributeQueryRequest, AttributeQuery> t1 = getHubAttributeQueryRequestToSamlAttributeQueryTransformer();
        Function<AttributeQuery, Element> t2 = getAttributeQueryToElementTransformer(keyStore, encryptionKeyStore, Optional.ofNullable(entity), signatureAlgorithm, digestAlgorithm, hubEntityId);

        return t2.compose(t1);
    }

    public Function<HubEidasAttributeQueryRequest, Element> getEidasMatchingServiceRequestToElementTransformer(
        IdaKeyStore keyStore,
        EncryptionKeyStore encryptionKeyStore,
        EntityToEncryptForLocator entity,
        SignatureAlgorithm signatureAlgorithm,
        DigestAlgorithm digestAlgorithm,
        String hubEntityId) {

        Function<HubEidasAttributeQueryRequest, AttributeQuery> t1 = getHubEidasAttributeQueryRequestToSamlAttributeQueryTransformer();
        Function<AttributeQuery, Element> t2 = getAttributeQueryToElementTransformer(keyStore, encryptionKeyStore, Optional.ofNullable(entity), signatureAlgorithm, digestAlgorithm, hubEntityId);

        return t2.compose(t1);
    }

    public Function<MatchingServiceHealthCheckRequest, Element> getMatchingServiceHealthCheckRequestToElementTransformer(
            IdaKeyStore keyStore,
            EncryptionKeyStore encryptionKeyStore,
            EntityToEncryptForLocator entity,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm, String hubEntityId) {
        Function<MatchingServiceHealthCheckRequest, AttributeQuery> t1
                = new MatchingServiceHealthCheckRequestToSamlAttributeQueryTransformer(new OpenSamlXmlObjectFactory());
        Function<AttributeQuery, Element> attributeQueryToElementTransformer = getAttributeQueryToElementTransformer(keyStore, encryptionKeyStore, Optional.ofNullable(entity), signatureAlgorithm, digestAlgorithm, hubEntityId);
        return attributeQueryToElementTransformer.compose(t1);
    }

    public <T extends RequestAbstractType> RequestAbstractTypeToStringTransformer<T> getRequestAbstractTypeToStringTransformer(
            boolean includeKeyInfo,
            IdaKeyStore keyStore,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm) {
        return new RequestAbstractTypeToStringTransformer<>(
                new SigningRequestAbstractTypeSignatureCreator<T>(new SignatureFactory(includeKeyInfo, new IdaKeyStoreCredentialRetriever(keyStore), signatureAlgorithm, digestAlgorithm)),
                new SamlSignatureSigner<T>(),
                new XmlObjectToBase64EncodedStringTransformer<>()
        );
    }

    public RequestAbstractTypeToStringTransformer<AuthnRequest> getAuthnRequestToStringTransformer(
            boolean includeKeyInfo,
            IdaKeyStore keyStore,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm) {
        return getRequestAbstractTypeToStringTransformer(includeKeyInfo, keyStore, signatureAlgorithm, digestAlgorithm);
    }

    public DecoratedSamlResponseToInboundResponseFromMatchingServiceTransformer getResponseToInboundResponseFromMatchingServiceTransformer(
            SigningKeyStore signingKeyStore,
            IdaKeyStore keyStore, String hubEntityId) {
        return new DecoratedSamlResponseToInboundResponseFromMatchingServiceTransformer(
                new InboundResponseFromMatchingServiceUnmarshaller(
                        getAssertionToPassthroughAssertionTransformer(),
                        new MatchingServiceIdaStatusUnmarshaller()
                ),
                getSamlResponseSignatureValidator(getSignatureValidator(signingKeyStore)),
                this.<InboundResponseFromMatchingService>getSamlResponseAssertionDecrypter(keyStore),
                getSamlAssertionsSignatureValidator(getSignatureValidator(signingKeyStore)),
                new EncryptedResponseFromMatchingServiceValidator(),
                new ResponseAssertionsFromMatchingServiceValidator(
                        new AssertionValidator(
                                new IssuerValidator(),
                                new AssertionSubjectValidator(),
                                new AssertionAttributeStatementValidator(),
                                new BasicAssertionSubjectConfirmationValidator()
                        ),
                        hubEntityId
                )
        );
    }

    /**
     * Compliance Tool should implement this method
     *
     * @deprecated Compliance Tool should implement this method
     */
    @Deprecated
    public Function<String, InboundResponseFromIdp> getStringToIdaResponseIssuedByIdpTransformer(
            SigningKeyStore signingKeyStore,
            IdaKeyStore keyStore,
            URI expectedDestinationHost,
            String expectedEndpoint,
            IdExpirationCache<String> assertionIdCache,
            String hubEntityId) {
        // not sure if we need to allow an extra ResponseSizeValidator here.
        Function<String, Response> t1 = getStringToResponseTransformer();
        Function<Response, InboundResponseFromIdp> t2 = getDecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(
                signingKeyStore,
                keyStore,
                expectedDestinationHost,
                expectedEndpoint,
                assertionIdCache,
                hubEntityId
        );
        return  t2.compose(t1);
    }

    public Function<String, InboundResponseFromIdp> getStringToIdaResponseIssuedByIdpTransformer(
            SignatureValidator idpSignatureValidator,
            IdaKeyStore keyStore,
            URI expectedDestinationHost,
            String expectedEndpoint,
            IdExpirationCache<String> assertionIdCache,
            String hubEntityId) {

        // not sure if we need to allow an extra ResponseSizeValidator here.
        Function<String, Response> t1 = getStringToResponseTransformer();
        Function<Response, InboundResponseFromIdp> t2 = getDecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(
                idpSignatureValidator,
                keyStore,
                expectedDestinationHost,
                expectedEndpoint,
                assertionIdCache,
                hubEntityId
        );
        return  t2.compose(t1);
    }

    /**
     * Compliance Tool should implement this method
     *
     * @deprecated Compliance Tool should implement this method
     */
    @Deprecated
    public DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer getDecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(
            SigningKeyStore signingKeyStore,
            IdaKeyStore keyStore,
            URI expectedDestinationHost,
            String expectedEndpoint,
            IdExpirationCache<String> assertionIdCache,
            String hubEntityId) {
        return getDecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(
                getSignatureValidator(signingKeyStore),
                keyStore,
                expectedDestinationHost,
                expectedEndpoint,
                assertionIdCache,
                hubEntityId
        );
    }

    public DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer getDecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(
            SignatureValidator idpSignatureValidator,
            IdaKeyStore keyStore,
            URI expectedDestinationHost,
            String expectedEndpoint,
            IdExpirationCache<String> assertionIdCache,
            String hubEntityId) {
        IdpResponseValidator validator = new IdpResponseValidator(this.getSamlResponseSignatureValidator(idpSignatureValidator),
            this.getSamlResponseAssertionDecrypter(keyStore),
                getSamlAssertionsSignatureValidator(idpSignatureValidator),
                new EncryptedResponseFromIdpValidator<>(new SamlStatusToIdaStatusCodeMapper()),
            new DestinationValidator(expectedDestinationHost, expectedEndpoint),
            getResponseAssertionsFromIdpValidator(assertionIdCache, hubEntityId));

        return new DecoratedSamlResponseToIdaResponseIssuedByIdpTransformer(
            validator,
            new IdaResponseFromIdpUnmarshaller(
                    new IdpIdaStatusUnmarshaller(),
                getAssertionToPassthroughAssertionTransformer()
            )
        );
    }

    public AuthnRequestToIdaRequestFromRelyingPartyTransformer getAuthnRequestToAuthnRequestFromTransactionTransformer(
        final URI expectedDestinationHost,
        final SigningKeyStore signingKeyStore,
        final IdaKeyStore decryptionKeyStore,
        final IdExpirationCache<AuthnRequestIdKey> duplicateIds,
        final SamlDuplicateRequestValidationConfiguration samlDuplicateRequestValidationConfiguration,
        final SamlAuthnRequestValidityDurationConfiguration samlAuthnRequestValidityDurationConfiguration
    ) {
        List<Credential> credential = new IdaKeyStoreCredentialRetriever(decryptionKeyStore).getDecryptingCredentials();
        Decrypter decrypter = new DecrypterFactory().createDecrypter(credential);

        return new AuthnRequestToIdaRequestFromRelyingPartyTransformer(
            new AuthnRequestFromRelyingPartyUnmarshaller(decrypter),
            coreTransformersFactory.getSamlRequestSignatureValidator(signingKeyStore),
            new DestinationValidator(expectedDestinationHost, Endpoints.SSO_REQUEST_ENDPOINT),
            new AuthnRequestFromTransactionValidator(
                new IssuerValidator(),
                new DuplicateAuthnRequestValidator(duplicateIds, samlDuplicateRequestValidationConfiguration),
                new AuthnRequestIssueInstantValidator(samlAuthnRequestValidityDurationConfiguration)
            )
        );
    }

    private OutboundResponseFromHubToSamlResponseTransformer getOutboundResponseFromHubToSamlResponseTransformer() {
        return new OutboundResponseFromHubToSamlResponseTransformer(
                new TransactionIdaStatusMarshaller(new OpenSamlXmlObjectFactory()),
                new OpenSamlXmlObjectFactory(),
                getEncryptedAssertionUnmarshaller());
    }

    private OutboundResponseFromHubToSamlResponseTransformer getSamlProfileOutboundResponseFromHubToSamlResponseTransformer() {
        return new OutboundResponseFromHubToSamlResponseTransformer(
                new SamlProfileTransactionIdaStatusMarshaller(new OpenSamlXmlObjectFactory()),
                new OpenSamlXmlObjectFactory(),
                getEncryptedAssertionUnmarshaller());
    }

    private HubIdentityProviderMetadataDtoToEntityDescriptorTransformer getHubIdentityProviderMetadataDtoToEntityDescriptorTransformer() {
        OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        return new HubIdentityProviderMetadataDtoToEntityDescriptorTransformer(
                openSamlXmlObjectFactory,
                coreTransformersFactory.getCertificatesToKeyDescriptorsTransformer(),
                new IdGenerator()
        );
    }

    private IdaAuthnRequestFromHubToAuthnRequestTransformer getIdaAuthnRequestFromHubToAuthnRequestTransformer() {
        return new IdaAuthnRequestFromHubToAuthnRequestTransformer(new OpenSamlXmlObjectFactory());
    }

    private EidasAuthnRequestFromHubToAuthnRequestTransformer getEidasAuthnRequestFromHubToAuthnRequestTransformer() {
        return new EidasAuthnRequestFromHubToAuthnRequestTransformer(new OpenSamlXmlObjectFactory(), new AuthnContextFactory());
    }

    private HubAttributeQueryRequestToSamlAttributeQueryTransformer getHubAttributeQueryRequestToSamlAttributeQueryTransformer() {
        return new HubAttributeQueryRequestToSamlAttributeQueryTransformer(
                new OpenSamlXmlObjectFactory(),
                new HubAssertionMarshaller(
                        new OpenSamlXmlObjectFactory(),
                        new AttributeFactory_1_1(new OpenSamlXmlObjectFactory()),
                        new OutboundAssertionToSubjectTransformer(new OpenSamlXmlObjectFactory())),
                new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory()),
                getEncryptedAssertionUnmarshaller());
    }

    private HubEidasAttributeQueryRequestToSamlAttributeQueryTransformer getHubEidasAttributeQueryRequestToSamlAttributeQueryTransformer() {
        return new HubEidasAttributeQueryRequestToSamlAttributeQueryTransformer(
                new OpenSamlXmlObjectFactory(),
                new HubAssertionMarshaller(
                        new OpenSamlXmlObjectFactory(),
                        new AttributeFactory_1_1(new OpenSamlXmlObjectFactory()),
                        new OutboundAssertionToSubjectTransformer(new OpenSamlXmlObjectFactory())),
                new AssertionFromIdpToAssertionTransformer(
                        getStringToAssertionTransformer()
                ),
                new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory()),
                getEncryptedAssertionUnmarshaller());
    }

    public EncryptedAssertionUnmarshaller getEncryptedAssertionUnmarshaller() {
        return new EncryptedAssertionUnmarshaller(getStringToEncryptedAssertionTransformer());
    }

    private StringToOpenSamlObjectTransformer<EncryptedAssertion> getStringToEncryptedAssertionTransformer() {
        return coreTransformersFactory.getStringtoOpenSamlObjectTransformer(
                new AssertionSizeValidator()
        );
    }

    private AttributeQueryToElementTransformer getAttributeQueryToElementTransformer(
            IdaKeyStore keyStore,
            EncryptionKeyStore encryptionKeyStore,
            Optional<EntityToEncryptForLocator> entity,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm,
            String hubEntityId) {
        return new AttributeQueryToElementTransformer(
                new SigningRequestAbstractTypeSignatureCreator<>(new SignatureFactory(new IdaKeyStoreCredentialRetriever(keyStore), signatureAlgorithm, digestAlgorithm)),
                new SamlAttributeQueryAssertionSignatureSigner(new IdaKeyStoreCredentialRetriever(keyStore), new OpenSamlXmlObjectFactory(), hubEntityId),
                new SamlSignatureSigner<AttributeQuery>(),
                new XmlObjectToElementTransformer<>(),
                getSamlAttributeQueryAssertionEncrypter(encryptionKeyStore, entity)
        );
    }

    private SamlAttributeQueryAssertionEncrypter getSamlAttributeQueryAssertionEncrypter(EncryptionKeyStore encryptionKeyStore, Optional<EntityToEncryptForLocator> entity) {
        return entity.map(entityToEncryptForLocator -> new SamlAttributeQueryAssertionEncrypter(new KeyStoreBackedEncryptionCredentialResolver(encryptionKeyStore), new EncrypterFactory(), entityToEncryptForLocator))
                .orElseGet(NoOpSamlAttributeQueryAssertionEncrypter::new);
    }

    private ResponseAssertionsFromIdpValidator getResponseAssertionsFromIdpValidator(final IdExpirationCache<String> assertionIdCache, String hubEntityId) {
        return new ResponseAssertionsFromIdpValidator(
                new IdentityProviderAssertionValidator(
                        new IssuerValidator(),
                        new AssertionSubjectValidator(),
                        new AssertionAttributeStatementValidator(),
                        new AssertionSubjectConfirmationValidator()
                ),
                new MatchingDatasetAssertionValidator(new DuplicateAssertionValidatorImpl(assertionIdCache)),
                new AuthnStatementAssertionValidator(
                        new DuplicateAssertionValidatorImpl(assertionIdCache)
                ),
                new IPAddressValidator(),
                hubEntityId
        );
    }

    public DecoratedSamlResponseToInboundHealthCheckResponseFromMatchingServiceTransformer getResponseInboundHealthCheckResponseFromMatchingServiceTransformer(SigningKeyStore signingKeyStore) {

        return new DecoratedSamlResponseToInboundHealthCheckResponseFromMatchingServiceTransformer(
            new InboundHealthCheckResponseFromMatchingServiceUnmarshaller(
                new MatchingServiceIdaStatusUnmarshaller()
            ),
            getSamlResponseSignatureValidator(getSignatureValidator(signingKeyStore)),
            new HealthCheckResponseFromMatchingServiceValidator(
            )
        );
    }

    private AssertionDecrypter getSamlResponseAssertionDecrypter(IdaKeyStore keyStore) {
        IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        DecrypterFactory decrypterFactory = new DecrypterFactory();
        Decrypter decrypter = decrypterFactory.createDecrypter(idaKeyStoreCredentialRetriever.getDecryptingCredentials());
        return new AssertionDecrypter(new EncryptionAlgorithmValidator(), decrypter);
    }

    private SignatureValidator getSignatureValidator(SigningKeyStore signingKeyStore) {
        SigningCredentialFactory signingCredentialFactory = new SigningCredentialFactory(signingKeyStore);
        return coreTransformersFactory.getSignatureValidator(signingCredentialFactory);
    }
    private SamlResponseSignatureValidator getSamlResponseSignatureValidator(SignatureValidator signatureValidator) {
        return new SamlResponseSignatureValidator(new SamlMessageSignatureValidator(signatureValidator));
    }

    private SamlAssertionsSignatureValidator getSamlAssertionsSignatureValidator(SignatureValidator signatureValidator) {
        return new SamlAssertionsSignatureValidator(new SamlMessageSignatureValidator(signatureValidator));
    }
}
