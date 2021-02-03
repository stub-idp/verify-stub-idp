package uk.gov.ida.matchingserviceadapter;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Environment;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.w3c.dom.Element;
import stubidp.saml.hub.validators.response.common.ResponseSizeValidator;
import stubidp.saml.metadata.DisabledMetadataResolverRepository;
import stubidp.saml.metadata.EidasMetadataConfiguration;
import stubidp.saml.metadata.EidasMetadataResolverRepository;
import stubidp.saml.metadata.EidasTrustAnchorHealthCheck;
import stubidp.saml.metadata.EidasTrustAnchorResolver;
import stubidp.saml.metadata.EidasValidatorFactory;
import stubidp.saml.metadata.ExpiredCertificateMetadataFilter;
import stubidp.saml.metadata.KeyStoreLoader;
import stubidp.saml.metadata.MetadataResolverConfigBuilder;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import stubidp.saml.metadata.MetadataResolverRepository;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.saml.metadata.factories.DropwizardMetadataResolverFactory;
import stubidp.saml.metadata.factories.MetadataSignatureTrustEngineFactory;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.DecrypterFactory;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.MetadataBackedEncryptionCredentialResolver;
import stubidp.saml.security.MetadataBackedSignatureValidator;
import stubidp.saml.security.PublicKeyFactory;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.SamlMessageSignatureValidator;
import stubidp.saml.security.SecretKeyDecryptorFactory;
import stubidp.saml.security.SecretKeyEncrypter;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import stubidp.saml.serializers.deserializers.ElementToOpenSamlXMLObjectTransformer;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.core.transformers.EidasMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.transformers.EidasUnsignedMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.transformers.VerifyMatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.transformers.inbound.Cycle3DatasetFactory;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import stubidp.saml.utils.core.validation.assertion.ExceptionThrowingValidator;
import stubidp.saml.utils.core.validation.conditions.AudienceRestrictionValidator;
import stubidp.saml.utils.metadata.transformers.KeyDescriptorsUnmarshaller;
import stubidp.utils.common.manifest.ManifestReader;
import stubidp.utils.rest.jerseyclient.ErrorHandlingClient;
import stubidp.utils.rest.jerseyclient.JsonClient;
import stubidp.utils.rest.jerseyclient.JsonResponseProcessor;
import stubidp.utils.security.configuration.DeserializablePublicKeyConfiguration;
import stubidp.utils.security.security.Certificate;
import stubidp.utils.security.security.IdGenerator;
import stubidp.utils.security.security.PublicKeyFileInputStreamFactory;
import stubidp.utils.security.security.PublicKeyInputStreamFactory;
import stubidp.utils.security.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.CertificateStore;
import uk.gov.ida.matchingserviceadapter.configuration.KeyPairConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.exceptions.ExceptionResponseFactory;
import uk.gov.ida.matchingserviceadapter.exceptions.InvalidCertificateException;
import uk.gov.ida.matchingserviceadapter.exceptions.MissingMetadataException;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingDatasetToMatchingDatasetDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceRequestDtoMapper;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxyImpl;
import uk.gov.ida.matchingserviceadapter.repositories.MatchingServiceAdapterMetadataRepository;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.api.MsaTransformersFactory;
import uk.gov.ida.matchingserviceadapter.services.AttributeQueryService;
import uk.gov.ida.matchingserviceadapter.services.EidasAssertionService;
import uk.gov.ida.matchingserviceadapter.services.MatchingResponseGenerator;
import uk.gov.ida.matchingserviceadapter.services.UnknownUserResponseGenerator;
import uk.gov.ida.matchingserviceadapter.services.VerifyAssertionService;
import uk.gov.ida.matchingserviceadapter.validators.AssertionTimeRestrictionValidator;
import uk.gov.ida.matchingserviceadapter.validators.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.validators.CountryConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.DateTimeComparator;
import uk.gov.ida.matchingserviceadapter.validators.IdpConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static stubidp.utils.security.security.Certificate.KeyUse.Encryption;
import static stubidp.utils.security.security.Certificate.KeyUse.Signing;

class MatchingServiceAdapterBinder extends AbstractBinder {

    private final MatchingServiceAdapterConfiguration configuration;
    private final Environment environment;
    private final MetadataResolverBundle<MatchingServiceAdapterConfiguration> metadataResolverBundle;

    public MatchingServiceAdapterBinder(MatchingServiceAdapterConfiguration configuration,
                                        Environment environment,
                                        MetadataResolverBundle<MatchingServiceAdapterConfiguration> metadataResolverBundle) {
        this.configuration = configuration;
        this.environment = environment;
        this.metadataResolverBundle = metadataResolverBundle;
    }

    @Override
    protected void configure() {

        bind(metadataResolverBundle.getMetadataResolverProvider().get()).to(MetadataResolver.class).in(Singleton.class);
        bind(metadataResolverBundle.getSignatureTrustEngineProvider()).to(ExplicitKeySignatureTrustEngine.class).in(Singleton.class);
        bind(metadataResolverBundle.getMetadataCredentialResolverProvider()).to(MetadataCredentialResolver.class).in(Singleton.class);

        bind(new SoapMessageManager()).to(SoapMessageManager.class);
        bindAsContract(X509CertificateFactory.class);
        bindAsContract(KeyStoreLoader.class);
        bindAsContract(IdGenerator.class);
        bindAsContract(ExpiredCertificateMetadataFilter.class);
        bindAsContract(ExceptionResponseFactory.class);
        bindAsContract(MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper.class);
        bindAsContract(MatchingServiceAdapterMetadataRepository.class);
        bindAsContract(UserAccountCreationAttributeExtractor.class);
        bindAsContract(UnknownUserResponseGenerator.class);

        bindAsContract(AssertionTimeRestrictionValidator.class);
        bindAsContract(SubjectValidator.class);
        bindAsContract(AudienceRestrictionValidator.class);
        bindAsContract(IdpConditionsValidator.class);
        bindAsContract(CountryConditionsValidator.class);

        bind(PublicKeyFileInputStreamFactory.class).to(PublicKeyInputStreamFactory.class).in(Singleton.class);
        bind(configuration).to(AssertionLifetimeConfiguration.class).in(Singleton.class);
        bind(configuration).to(MatchingServiceAdapterConfiguration.class).in(Singleton.class);
        bind(MatchingServiceProxyImpl.class).to(MatchingServiceProxy.class).in(Singleton.class);
        final ManifestReader manifestReader = new ManifestReader();
        bind(manifestReader).to(ManifestReader.class);
        bind(new MatchingDatasetToMatchingDatasetDtoMapper()).to(MatchingDatasetToMatchingDatasetDtoMapper.class);

        final DateTimeComparator dateTimeComparator = new DateTimeComparator(Duration.ofSeconds(configuration.getClockSkew()));
        bind(dateTimeComparator).to(DateTimeComparator.class).in(Singleton.class);

        final IdaKeyStore idaKeyStore = getIdaKeyStore(configuration);
        bind(idaKeyStore).to(IdaKeyStore.class);

        final ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = metadataResolverBundle.getSignatureTrustEngineProvider().get();
        bind(explicitKeySignatureTrustEngine).to(ExplicitKeySignatureTrustEngine.class);
        final MetadataBackedSignatureValidator metadataBackedSignatureValidator = MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
        bind(metadataBackedSignatureValidator).to(MetadataBackedSignatureValidator.class);

        final SamlMessageSignatureValidator samlMessageSignatureValidator = new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
        final SamlAssertionsSignatureValidator samlAssertionsSignatureValidator = new SamlAssertionsSignatureValidator(samlMessageSignatureValidator);

        bindAsContract(EidasMatchingDatasetUnmarshaller.class).in(Singleton.class);

        final String hubEntityId = configuration.getHubEntityId();
        bind(hubEntityId).to(String.class).named("HubEntityId").in(Singleton.class);

        final EidasTrustAnchorResolver trustAnchorResolver;
        if (!configuration.isEidasEnabled()) {
            trustAnchorResolver = null;
        } else {
            final Client trustAnchorClient = new JerseyClientBuilder(environment)
                    .using(configuration.getEuropeanIdentity().getAggregatedMetadata().getJerseyClientConfiguration())
                    .build(configuration.getEuropeanIdentity().getAggregatedMetadata().getJerseyClientName());

            EidasMetadataConfiguration metadataConfiguration = configuration.getEuropeanIdentity().getAggregatedMetadata();
            trustAnchorResolver = new EidasTrustAnchorResolver(metadataConfiguration.getTrustAnchorUri(), trustAnchorClient, metadataConfiguration.getTrustStore());
        }

        final Client eidasMetadataClient = new JerseyClientBuilder(environment).using(configuration.getMatchingServiceClientConfiguration()).build("EidasMetadataClient");

        final MetadataResolverRepository metadataResolverRepository;
        if (!configuration.isEidasEnabled()) {
            metadataResolverRepository = new DisabledMetadataResolverRepository();
        } else {
            final EidasMetadataResolverRepository eidasMetadataResolverRepository = new EidasMetadataResolverRepository(
                    trustAnchorResolver,
                    configuration.getEuropeanIdentity().getAggregatedMetadata(),
                    new DropwizardMetadataResolverFactory(),
                    new Timer(),
                    new MetadataSignatureTrustEngineFactory(),
                    new MetadataResolverConfigBuilder(),
                    eidasMetadataClient
            );
            metadataResolverRepository = eidasMetadataResolverRepository;
            registerMetadataRefreshTask(environment, Optional.of(eidasMetadataResolverRepository), Collections.unmodifiableCollection(eidasMetadataResolverRepository.getMetadataResolvers().values()), "eidas-metadata");
            environment.healthChecks().register("TrustAnchorHealthCheck", new EidasTrustAnchorHealthCheck(eidasMetadataResolverRepository));
        }
        bind(metadataResolverRepository).to(MetadataResolverRepository.class);

        final InstantValidator instantValidator = new InstantValidator(dateTimeComparator);
        bind(instantValidator).to(InstantValidator.class);
        final AssertionTimeRestrictionValidator assertionTimeRestrictionValidator = new AssertionTimeRestrictionValidator(dateTimeComparator);
        bind(assertionTimeRestrictionValidator).to(AssertionTimeRestrictionValidator.class);
        final SubjectValidator subjectValidator = new SubjectValidator(assertionTimeRestrictionValidator);

        StringToOpenSamlObjectTransformer<Response> stringtoOpenSamlObjectTransformer = new CoreTransformersFactory().getStringtoOpenSamlObjectTransformer(new ResponseSizeValidator());
        EidasValidatorFactory eidasValidatorFactory = new EidasValidatorFactory(metadataResolverRepository);
        final List<String> allAcceptableHubConnectorEntityIds = configuration.isEidasEnabled() ? configuration.getEuropeanIdentity().getAllAcceptableHubConnectorEntityIds(configuration.getMetadataEnvironment()) : Collections.emptyList();
        bind(allAcceptableHubConnectorEntityIds).to(new GenericType<List<String>>() {}).named("AllAcceptableHubConnectorEntityIds").in(Singleton.class);

        final AudienceRestrictionValidator audienceRestrictionValidator = new AudienceRestrictionValidator();
        final CountryConditionsValidator conditionsValidator = new CountryConditionsValidator(assertionTimeRestrictionValidator, audienceRestrictionValidator);

        ExceptionThrowingValidator<Assertion> eidasSamlValidator = a -> {
            try {
                instantValidator.validate(a.getIssueInstant(), "Country Assertion IssueInstant");
                // FIXME: the expectedInResponseTo param is never used in the subjectValidator
                subjectValidator.validate(a.getSubject(), a.getID());
                conditionsValidator.validate(a.getConditions(), allAcceptableHubConnectorEntityIds.toArray(new String[0]));
            } catch (SamlResponseValidationException e) {
                throw new ExceptionThrowingValidator.ValidationException("Error validating assertion " + a.getID(), e);
            }
        };

        final MetadataBackedEncryptionCredentialResolver metadataBackedEncryptionCredentialResolver = new MetadataBackedEncryptionCredentialResolver(metadataResolverBundle.getMetadataCredentialResolver(), SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        bind(metadataBackedEncryptionCredentialResolver)
                .to(MetadataBackedEncryptionCredentialResolver.class).in(Singleton.class);
        final IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(idaKeyStore);
        bind(idaKeyStoreCredentialRetriever).to(IdaKeyStoreCredentialRetriever.class).in(Singleton.class);

        bind(new SecretKeyEncrypter(metadataBackedEncryptionCredentialResolver)).to(SecretKeyEncrypter.class).in(Singleton.class);
        bind(new SecretKeyDecryptorFactory(idaKeyStoreCredentialRetriever)).to(SecretKeyDecryptorFactory.class).in(Singleton.class);

        final SecretKeyDecryptorFactory secretKeyDecryptorFactory = new SecretKeyDecryptorFactory(idaKeyStoreCredentialRetriever);
        final EidasUnsignedMatchingDatasetUnmarshaller eidasUnsignedMatchingDatasetUnmarshaller = new EidasUnsignedMatchingDatasetUnmarshaller(
                secretKeyDecryptorFactory,
                stringtoOpenSamlObjectTransformer,
                eidasValidatorFactory,
                eidasSamlValidator);
        bind(eidasUnsignedMatchingDatasetUnmarshaller).to(EidasUnsignedMatchingDatasetUnmarshaller.class);

        final UserIdHashFactory userIdHashFactory = new UserIdHashFactory(configuration.getEntityId());
        bind(userIdHashFactory).to(UserIdHashFactory.class).in(Singleton.class);

        bindAsContract(Cycle3DatasetFactory.class).in(Singleton.class);

        bind(new MatchingServiceRequestDtoMapper(new MatchingDatasetToMatchingDatasetDtoMapper(), configuration.isEidasEnabled()))
                .to(MatchingServiceRequestDtoMapper.class).in(Singleton.class);

        final AttributeQuerySignatureValidator attributeQuerySignatureValidator = new AttributeQuerySignatureValidator(samlMessageSignatureValidator);
        bind(attributeQuerySignatureValidator).to(AttributeQuerySignatureValidator.class);

        bind(configuration.getMetadataConfiguration().orElseThrow(MissingMetadataException::new).getHubFederationId())
                .to(String.class).named("HubFederationId").in(Singleton.class);
        final EntityToEncryptForLocator entityToEncryptForLocator = requestId -> hubEntityId;
        bind(entityToEncryptForLocator).to(EntityToEncryptForLocator.class).in(Singleton.class);

        final Cycle3DatasetFactory cycle3DatasetFactory = new Cycle3DatasetFactory();
        final VerifyAssertionService verifyAssertionService = new VerifyAssertionService(instantValidator,
                subjectValidator,
                new IdpConditionsValidator(assertionTimeRestrictionValidator, audienceRestrictionValidator),
                samlAssertionsSignatureValidator,
                cycle3DatasetFactory,
                hubEntityId,
                new VerifyMatchingDatasetUnmarshaller());

        final EidasAssertionService eidasAssertionService = new EidasAssertionService(instantValidator,
                subjectValidator,
                conditionsValidator,
                samlAssertionsSignatureValidator,
                cycle3DatasetFactory,
                metadataResolverRepository,
                allAcceptableHubConnectorEntityIds,
                hubEntityId,
                new EidasMatchingDatasetUnmarshaller(),
                eidasUnsignedMatchingDatasetUnmarshaller);

        bind(eidasAssertionService).to(EidasAssertionService.class);

        final AttributeQueryService attributeQueryService = new AttributeQueryService(attributeQuerySignatureValidator, instantValidator, verifyAssertionService, eidasAssertionService, userIdHashFactory, hubEntityId);
        bind(attributeQueryService).to(AttributeQueryService.class);

        MsaTransformersFactory msaTransformersFactory = new MsaTransformersFactory();
        final Function<OutboundResponseFromMatchingService, Element> outboundResponseFromMatchingServiceToElementTransformer = msaTransformersFactory.getOutboundResponseFromMatchingServiceToElementTransformer(
                metadataBackedEncryptionCredentialResolver,
                idaKeyStore,
                entityToEncryptForLocator,
                configuration
        );
        final Function<HealthCheckResponseFromMatchingService, Element> healthcheckResponseFromMatchingServiceToElementTransformer = msaTransformersFactory.getHealthcheckResponseFromMatchingServiceToElementTransformer(
                metadataBackedEncryptionCredentialResolver,
                idaKeyStore,
                entityToEncryptForLocator,
                configuration
        );
        final MatchingResponseGenerator matchingResponseGenerator = new MatchingResponseGenerator(new SoapMessageManager(),
                outboundResponseFromMatchingServiceToElementTransformer,
                healthcheckResponseFromMatchingServiceToElementTransformer,
                manifestReader,
                configuration);
        bind(matchingResponseGenerator).to(MatchingResponseGenerator.class);

        bindAsContract(OpenSamlXmlObjectFactory.class);
        bindAsContract(PublicKeyFactory.class);

        Client client = new JerseyClientBuilder(environment).using(configuration.getMatchingServiceClientConfiguration()).build("MatchingServiceClient");
        final JsonClient matchingServiceClient = new JsonClient(new ErrorHandlingClient(client), new JsonResponseProcessor(environment.getObjectMapper()));
        bind(matchingServiceClient).to(JsonClient.class).named("MatchingServiceClient").in(Singleton.class);

        bind(configuration.getEncryptionKeys().get(0).getPublicKey()).to(DeserializablePublicKeyConfiguration.class);

        bind(getCertificateStore(configuration)).to(CertificateStore.class).in(Singleton.class);

        final Function<OutboundResponseFromUnknownUserCreationService, Element> outboundResponseFromUnknownUserCreationServiceToElementTransformer = msaTransformersFactory.getOutboundResponseFromUnknownUserCreationServiceToElementTransformer(
                metadataBackedEncryptionCredentialResolver,
                idaKeyStore,
                entityToEncryptForLocator,
                configuration
        );
        bind(outboundResponseFromUnknownUserCreationServiceToElementTransformer).to(new GenericType<Function<OutboundResponseFromUnknownUserCreationService, Element>>(){});

        Decrypter decrypter = new DecrypterFactory().createDecrypter(idaKeyStoreCredentialRetriever.getDecryptingCredentials());
        bind(new AssertionDecrypter(new EncryptionAlgorithmValidator(), decrypter)).to(AssertionDecrypter.class);

        final CoreTransformersFactory coreTransformersFactory = new CoreTransformersFactory();
        bind(coreTransformersFactory.getElementToOpenSamlXmlObjectTransformer())
                .to(new GenericType<ElementToOpenSamlXMLObjectTransformer<AttributeQuery>>() {}).in(Singleton.class);
        bind(coreTransformersFactory.getElementToOpenSamlXmlObjectTransformer())
                .to(new GenericType<ElementToOpenSamlXMLObjectTransformer<EntityDescriptor>>() {}).in(Singleton.class);
        bind(coreTransformersFactory.getCertificatesToKeyDescriptorsTransformer())
                .to(KeyDescriptorsUnmarshaller.class).in(Singleton.class);
        bind(coreTransformersFactory.getXmlObjectToElementTransformer())
                .to(new GenericType<Function<EntitiesDescriptor, Element>>() {}).in(Singleton.class);

        bind(configuration.getMetadataConfiguration()).to(new GenericType<Optional<MetadataResolverConfiguration>>() {}).in(Singleton.class);

    }

    public IdaKeyStore getIdaKeyStore(MatchingServiceAdapterConfiguration configuration) {
        List<KeyPair> encryptionKeyPairs = configuration.getEncryptionKeys().stream()
                .map(pair -> new KeyPair(pair.getPublicKey().getPublicKey(), pair.getPrivateKey().getPrivateKey()))
                .collect(Collectors.toList());

        KeyPair signingKeyPair = new KeyPair(
                configuration.getSigningKeys().get(0).getPublicKey().getPublicKey(),
                configuration.getSigningKeys().get(0).getPrivateKey().getPrivateKey()
        );

        return new IdaKeyStore(signingKeyPair, encryptionKeyPairs);
    }

    public CertificateStore getCertificateStore(MatchingServiceAdapterConfiguration configuration) {
        List<Certificate> publicSigningCertificates = configuration.getSigningKeys().stream()
                .map(KeyPairConfiguration::getPublicKey)
                .map(key -> cert(key.getName(), key.getCert(), Signing))
                .collect(Collectors.toList());

        List<Certificate> publicEncryptionCertificates = Stream.of(configuration.getEncryptionKeys().get(0).getPublicKey())
                .map(key -> cert(key.getName(), key.getCert(), Encryption))
                .collect(Collectors.toList());

        return new CertificateStore(
                publicEncryptionCertificates,
                publicSigningCertificates);
    }

    public Certificate cert(String keyName, String cert, Certificate.KeyUse keyUse) {
        try {
            X509Certificate x509cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(cert.getBytes()));
            String certBody = Base64.encodeBase64String(x509cert.getEncoded());
            return new Certificate(keyName, certBody, keyUse);
        } catch (CertificateException e) {
            throw new InvalidCertificateException(e);
        }
    }

    public static void registerMetadataRefreshTask(Environment environment, Optional<EidasMetadataResolverRepository> eidasMetadataResolverRepository, Collection<MetadataResolver> metadataResolvers, String name) {
        environment.admin().addTask(new Task(name + "-refresh") {
            @Override
            public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
                for(MetadataResolver metadataResolver : metadataResolvers) {
                    if(metadataResolver instanceof AbstractReloadingMetadataResolver abstractReloadingMetadataResolver) {
                        abstractReloadingMetadataResolver.refresh();
                    }
                }
                eidasMetadataResolverRepository.ifPresent(EidasMetadataResolverRepository::refresh);
            }
        });
    }
}
