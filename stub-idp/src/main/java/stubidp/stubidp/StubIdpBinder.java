package stubidp.stubidp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.jdbi.v3.core.Jdbi;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.metadata.MetadataHealthCheck;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import stubidp.saml.metadata.factories.DropwizardMetadataResolverFactory;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.security.SigningKeyStore;
import stubidp.saml.security.signature.SignatureRSASSAPSS;
import stubidp.saml.stubidp.configuration.SamlConfiguration;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.hub.domain.IdaAuthnRequestFromHub;
import stubidp.stubidp.auth.ManagedAuthFilterInstaller;
import stubidp.stubidp.builders.CountryMetadataBuilder;
import stubidp.stubidp.builders.CountryMetadataSigningHelper;
import stubidp.stubidp.configuration.AssertionLifetimeConfiguration;
import stubidp.stubidp.configuration.IdpStubsConfiguration;
import stubidp.stubidp.configuration.SigningKeyPairConfiguration;
import stubidp.stubidp.configuration.SingleIdpConfiguration;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.cookies.CookieFactory;
import stubidp.stubidp.cookies.HmacValidator;
import stubidp.stubidp.domain.factories.AssertionFactory;
import stubidp.stubidp.domain.factories.AssertionRestrictionsFactory;
import stubidp.stubidp.domain.factories.IdentityProviderAssertionFactory;
import stubidp.stubidp.domain.factories.StubTransformersFactory;
import stubidp.stubidp.listeners.StubIdpsFileListener;
import stubidp.stubidp.repositories.AllIdpsUserRepository;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.repositories.MetadataRepository;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.stubidp.repositories.UserRepository;
import stubidp.stubidp.repositories.jdbc.JDBIEidasSessionRepository;
import stubidp.stubidp.repositories.jdbc.JDBIIdpSessionRepository;
import stubidp.stubidp.repositories.jdbc.JDBIUserRepository;
import stubidp.stubidp.repositories.jdbc.UserMapper;
import stubidp.stubidp.repositories.reaper.ManagedStaleSessionReaper;
import stubidp.stubidp.saml.locators.IdpHardCodedEntityToEncryptForLocator;
import stubidp.stubidp.saml.transformers.EidasResponseTransformerProvider;
import stubidp.stubidp.saml.transformers.OutboundResponseFromIdpTransformerProvider;
import stubidp.stubidp.security.HubEncryptionKeyStore;
import stubidp.stubidp.security.IdaAuthnRequestKeyStore;
import stubidp.stubidp.services.AuthnRequestReceiverService;
import stubidp.stubidp.services.EidasAuthnResponseService;
import stubidp.stubidp.services.GeneratePasswordService;
import stubidp.stubidp.services.IdpUserService;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.services.ServiceListService;
import stubidp.stubidp.services.StubCountryService;
import stubidp.stubidp.services.SuccessAuthnResponseService;
import stubidp.stubidp.services.UserService;
import stubidp.stubidp.views.SamlResponseRedirectViewFactory;
import stubidp.utils.rest.jerseyclient.ErrorHandlingClient;
import stubidp.utils.rest.jerseyclient.JsonClient;
import stubidp.utils.rest.jerseyclient.JsonResponseProcessor;
import stubidp.utils.rest.restclient.ClientProvider;
import stubidp.utils.rest.truststore.EmptyKeyStoreProvider;
import stubidp.utils.security.configuration.SecureCookieConfiguration;
import stubidp.utils.security.configuration.SecureCookieKeyStore;
import stubidp.utils.security.security.HmacDigest;
import stubidp.utils.security.security.IdGenerator;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.SecureCookieKeyConfigurationKeyStore;
import stubidp.utils.security.security.X509CertificateFactory;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class StubIdpBinder extends AbstractBinder {

    public static final String HUB_METADATA_REPOSITORY = "HubMetadataRepository";
    public static final String HUB_METADATA_RESOLVER = "HubMetadataResolver";
    public static final String HUB_ENCRYPTION_KEY_STORE = "HubEncryptionKeyStore";
    public static final String IDP_SIGNING_KEY_STORE = "IdpSigningKeyStore";
    public static final String HUB_ENTITY_ID = "HubEntityId";

    public static final String HUB_CONNECTOR_METADATA_RESOLVER = "HubConnectorMetadataResolver";
    public static final String HUB_CONNECTOR_METADATA_REPOSITORY = "HubConnectorMetadataRepository";
    public static final String HUB_CONNECTOR_ENCRYPTION_KEY_STORE = "HubConnectorEncryptionKeyStore";
    public static final String HUB_CONNECTOR_ENTITY_ID = "HubConnectorEntityId";
    public static final String STUB_COUNTRY_METADATA_URL = "StubCountryMetadataUrl";
    public static final String STUB_COUNTRY_SSO_URL = "StubCountrySsoUrl";
    public static final String COUNTRY_SIGNING_KEY_STORE = "CountrySigningKeyStore";
    public static final String COUNTRY_METADATA_SIGNATURE_FACTORY = "countryMetadataSignatureFactory";
    public static final String COUNTRY_METADATA_VALIDITY_PERIOD = "metadataValidityPeriod";
    public static final String RSASHA256_EIDAS_AUTHN_RESPONSE_SERVICE = "RSASHA256EidasAuthnResponseService";
    public static final String RSASSAPSS_EIDAS_AUTHN_RESPONSE_SERVICE = "RSASSAPSSEidasAuthnResponseService";
    private final String RSASHA256_EIDAS_RESPONSE_TRANSFORMER_PROVIDER = "RSASHA256EidasResponseTransfomerProvider";
    private final String RSASSAPSS_EIDAS_RESPONSE_TRANSFORMER_PROVIDER = "RSASSAPSSEidasResponseTransformerProvider";

    // unused?
    private final String SESSION_CACHE_TIMEOUT_IN_MINUTES = "sessionCacheTimeoutInMinutes";

    public static final String IS_SECURE_COOKIE_ENABLED = "isSecureCookieEnabled";

    private final StubIdpConfiguration stubIdpConfiguration;
    private final Environment environment;

    public StubIdpBinder(StubIdpConfiguration stubIdpConfiguration,
                         Environment environment) {
        this.stubIdpConfiguration = stubIdpConfiguration;
        this.environment = environment;
    }

    @Override
    protected void configure() {

        // idp configuration
        bind(stubIdpConfiguration).to(AssertionLifetimeConfiguration.class);
        bind(stubIdpConfiguration.getSamlConfiguration()).to(SamlConfiguration.class);
        final String hubEntityId = stubIdpConfiguration.getHubEntityId();
        bind(hubEntityId).named(HUB_ENTITY_ID).to(String.class);
        bind(180).named(SESSION_CACHE_TIMEOUT_IN_MINUTES).to(Integer.class);

        final IdpHardCodedEntityToEncryptForLocator entityToEncryptForLocator = new IdpHardCodedEntityToEncryptForLocator(hubEntityId);
        bind(entityToEncryptForLocator).to(EntityToEncryptForLocator.class);
        bind(PublicKeyFactory.class).to(PublicKeyFactory.class);
        final IdaKeyStore idpSigningKeyStore = getKeystoreFromConfig(stubIdpConfiguration.getSigningKeyPairConfiguration());
        bind(idpSigningKeyStore).named(IDP_SIGNING_KEY_STORE).to(IdaKeyStore.class);

        final MetadataResolver idpMetadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolver(environment, stubIdpConfiguration.getMetadataConfiguration());
        registerMetadataHealthcheckAndRefresh(environment, idpMetadataResolver, stubIdpConfiguration.getMetadataConfiguration(), "metadata");
        bind(idpMetadataResolver).named(HUB_METADATA_RESOLVER).to(MetadataResolver.class);
        final MetadataRepository idpMetadataRepository = new MetadataRepository(idpMetadataResolver, hubEntityId);
        bind(idpMetadataRepository).named(HUB_METADATA_REPOSITORY).to(MetadataRepository.class);
        final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        final HubEncryptionKeyStore hubEncryptionKeyStore = new HubEncryptionKeyStore(idpMetadataRepository, publicKeyFactory);
        bind(hubEncryptionKeyStore).named(HUB_ENCRYPTION_KEY_STORE).to(EncryptionKeyStore.class);

        final IdaAuthnRequestKeyStore signingKeyStore = new IdaAuthnRequestKeyStore(idpMetadataRepository, publicKeyFactory);
        bind(signingKeyStore).to(SigningKeyStore.class);

        bind(SamlResponseRedirectViewFactory.class).to(SamlResponseRedirectViewFactory.class);
        bind(AssertionFactory.class).to(AssertionFactory.class);
        bind(AssertionRestrictionsFactory.class).to(AssertionRestrictionsFactory.class);
        bind(IdentityProviderAssertionFactory.class).to(IdentityProviderAssertionFactory.class);
        bind(IdGenerator.class).to(IdGenerator.class);
        bind(X509CertificateFactory.class).to(X509CertificateFactory.class);
        final SignatureRSASHA256 signatureAlgorithm = new SignatureRSASHA256();
        bind(signatureAlgorithm).to(SignatureAlgorithm.class);
        final DigestSHA256 digestAlgorithm = new DigestSHA256();
        bind(digestAlgorithm).to(DigestAlgorithm.class);
        final StubTransformersFactory stubTransformersFactory = new StubTransformersFactory();
        bind(stubTransformersFactory.getStringToAuthnRequest()).to(new GenericType<Function<String, AuthnRequest>>() {});
        bind(stubTransformersFactory.getStringToIdaAuthnRequestFromHub(signingKeyStore)).to(new GenericType<Function<String, IdaAuthnRequestFromHub>>() {});
        bind(new OutboundResponseFromIdpTransformerProvider(hubEncryptionKeyStore,
                    idpSigningKeyStore, entityToEncryptForLocator,
                    Optional.ofNullable(stubIdpConfiguration.getSigningKeyPairConfiguration().getCert()),
                    stubTransformersFactory, signatureAlgorithm, digestAlgorithm)).to(OutboundResponseFromIdpTransformerProvider.class);

        bind(AllIdpsUserRepository.class).in(Singleton.class).to(AllIdpsUserRepository.class);
        bind(IdpStubsRepository.class).in(Singleton.class).to(IdpStubsRepository.class);
        bind(StubIdpsFileListener.class).in(Singleton.class).to(StubIdpsFileListener.class);
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        final UserMapper userMapper = new UserMapper(objectMapper);
        bind(userMapper).to(UserMapper.class);
        final Jdbi jdbi = Jdbi.create(stubIdpConfiguration.getDatabaseConfiguration().getUrl());
        bind(jdbi).to(Jdbi.class);
        bind(JDBIUserRepository.class).in(Singleton.class).to(UserRepository.class);
        bind(JDBIIdpSessionRepository.class).in(Singleton.class).to(IdpSessionRepository.class);
        bind(JDBIEidasSessionRepository.class).in(Singleton.class).to(EidasSessionRepository.class);
        bind(StubCountryRepository.class).in(Singleton.class).to(StubCountryRepository.class);

//        bind(KeyStore.class).toProvider(EmptyKeyStoreProvider.class).asEagerSingleton();
        bind(new EmptyKeyStoreProvider().get()).to(KeyStore.class);

        //must be eager singletons to be auto injected
        // Elegant-hack: this is how we install the basic auth filter, so we can use a guice injected user repository
        bind(ManagedAuthFilterInstaller.class).in(Singleton.class).to(ManagedAuthFilterInstaller.class);

        bind(AuthnRequestReceiverService.class).to(AuthnRequestReceiverService.class);
        bind(SuccessAuthnResponseService.class).to(SuccessAuthnResponseService.class);
        bind(GeneratePasswordService.class).to(GeneratePasswordService.class);
        bind(NonSuccessAuthnResponseService.class).to(NonSuccessAuthnResponseService.class);
        bind(IdpUserService.class).to(IdpUserService.class);
        bind(UserService.class).to(UserService.class);
        bind(SamlResponseRedirectViewFactory.class).to(SamlResponseRedirectViewFactory.class);

        bind(ManagedStaleSessionReaper.class).in(Singleton.class).to(ManagedStaleSessionReaper.class);

        // eidas configuration
        final String hubConnectorEntityId = stubIdpConfiguration.getEuropeanIdentityConfiguration().getHubConnectorEntityId();
        bind(hubConnectorEntityId).named(HUB_CONNECTOR_ENTITY_ID).to(String.class);
        final String stubCountryMetadataUrl = stubIdpConfiguration.getEuropeanIdentityConfiguration().getStubCountryBaseUrl() + Urls.METADATA_RESOURCE;
        bind(stubCountryMetadataUrl).named(STUB_COUNTRY_METADATA_URL).to(String.class);
        final String stubCountrySsoUrl = stubIdpConfiguration.getEuropeanIdentityConfiguration().getStubCountryBaseUrl() + Urls.EIDAS_SAML2_SSO_RESOURCE;
        bind(stubCountrySsoUrl).named(STUB_COUNTRY_SSO_URL).to(String.class);
        bind(new Period().withYears(100)).named(COUNTRY_METADATA_VALIDITY_PERIOD).to(ReadablePeriod.class);

        final IdaKeyStore countryKeyStore = getKeystoreFromConfig(stubIdpConfiguration.getEuropeanIdentityConfiguration().getSigningKeyPairConfiguration());
        bind(countryKeyStore).named(COUNTRY_SIGNING_KEY_STORE).to(IdaKeyStore.class);
        final SignatureFactory signatureFactory = new SignatureFactory(true, new IdaKeyStoreCredentialRetriever(countryKeyStore), signatureAlgorithm, digestAlgorithm);
        bind(signatureFactory).named(COUNTRY_METADATA_SIGNATURE_FACTORY).to(SignatureFactory.class);

        final Optional<MetadataRepository> eidasMetadataRepository;
        final Optional<HubEncryptionKeyStore> eidasHubEncryptionKeyStore;
        if (stubIdpConfiguration.getEuropeanIdentityConfiguration().isEnabled()) {
            final MetadataResolver metadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolver(environment, stubIdpConfiguration.getEuropeanIdentityConfiguration().getMetadata());
            registerMetadataHealthcheckAndRefresh(environment, metadataResolver, stubIdpConfiguration.getEuropeanIdentityConfiguration().getMetadata(), "connector-metadata");
            bind(Optional.of(metadataResolver)).named(HUB_CONNECTOR_METADATA_RESOLVER).to(new GenericType<Optional<MetadataResolver>>() {});
            eidasMetadataRepository = Optional.of(new MetadataRepository(metadataResolver, hubConnectorEntityId));
            bind(eidasMetadataRepository).named(HUB_CONNECTOR_METADATA_REPOSITORY).to(new GenericType<Optional<MetadataRepository>>() {});
            eidasHubEncryptionKeyStore = Optional.of(new HubEncryptionKeyStore(eidasMetadataRepository.get(), publicKeyFactory));
            bind(eidasHubEncryptionKeyStore).named(HUB_CONNECTOR_ENCRYPTION_KEY_STORE).to(new GenericType<Optional<EncryptionKeyStore>>() {});
        } else {
            bind(Optional.empty()).named(HUB_CONNECTOR_METADATA_RESOLVER).to(new GenericType<Optional<MetadataResolver>>() {});
            eidasMetadataRepository = Optional.empty();
            bind(eidasMetadataRepository).named(HUB_CONNECTOR_METADATA_REPOSITORY).to(new GenericType<Optional<MetadataRepository>>() {});
            eidasHubEncryptionKeyStore = Optional.empty();
            bind(eidasHubEncryptionKeyStore).named(HUB_CONNECTOR_ENCRYPTION_KEY_STORE).to(new GenericType<Optional<EncryptionKeyStore>>() {});
        }

        final CoreTransformersFactory coreTransformersFactory = new CoreTransformersFactory();
        final EidasResponseTransformerProvider sha256EidasResponseTransformerProvider = new EidasResponseTransformerProvider(
                coreTransformersFactory,
                eidasHubEncryptionKeyStore.orElse(null),
                countryKeyStore,
                entityToEncryptForLocator,
                signatureAlgorithm,
                digestAlgorithm);
        bind(sha256EidasResponseTransformerProvider).named(RSASHA256_EIDAS_RESPONSE_TRANSFORMER_PROVIDER).to(EidasResponseTransformerProvider.class);
        bind(new EidasAuthnResponseService(hubConnectorEntityId, sha256EidasResponseTransformerProvider, eidasMetadataRepository,
                stubCountryMetadataUrl)).named(RSASHA256_EIDAS_AUTHN_RESPONSE_SERVICE).to(EidasAuthnResponseService.class);
        final EidasResponseTransformerProvider rsassapaaEidasResponseTransformerProvider = new EidasResponseTransformerProvider(
                coreTransformersFactory,
                eidasHubEncryptionKeyStore.orElse(null),
                countryKeyStore,
                entityToEncryptForLocator,
                new SignatureRSASSAPSS(),
                digestAlgorithm);
        bind(rsassapaaEidasResponseTransformerProvider).named(RSASSAPSS_EIDAS_RESPONSE_TRANSFORMER_PROVIDER).to(EidasResponseTransformerProvider.class);
        bind(new EidasAuthnResponseService(hubConnectorEntityId, rsassapaaEidasResponseTransformerProvider, eidasMetadataRepository,
                stubCountryMetadataUrl)).named(RSASSAPSS_EIDAS_AUTHN_RESPONSE_SERVICE).to(EidasAuthnResponseService.class);


        bind(CountryMetadataSigningHelper.class).in(Singleton.class).to(CountryMetadataSigningHelper.class);
        bind(CountryMetadataBuilder.class).to(CountryMetadataBuilder.class);

        bind(StubCountryService.class).to(StubCountryService.class);

        bind(JsonResponseProcessor.class).to(JsonResponseProcessor.class);

        // single idp stuff
        bind(stubIdpConfiguration.getSingleIdpJourneyConfiguration()).to(SingleIdpConfiguration.class);

        final Client client = new ClientProvider(environment,
                stubIdpConfiguration.getSingleIdpJourneyConfiguration().getServiceListClient(),
                true, "StubIdpJsonClient").get();
        final JsonClient jsonClient = new JsonClient(new ErrorHandlingClient(client), new JsonResponseProcessor(objectMapper));
        bind(new ServiceListService(stubIdpConfiguration.getSingleIdpJourneyConfiguration(), jsonClient)).to(ServiceListService.class);

        // secure cookie config
        final boolean isSecureCookieEnabled = Objects.nonNull(stubIdpConfiguration.getSecureCookieConfiguration());
        bind(isSecureCookieEnabled).named(IS_SECURE_COOKIE_ENABLED).to(Boolean.class);
        if(isSecureCookieEnabled) {
            bind(stubIdpConfiguration.getSecureCookieConfiguration()).to(SecureCookieConfiguration.class);
        } else {
            bind(new SecureCookieConfiguration() {{
                this.secure = false;
            }}).to(SecureCookieConfiguration.class);
        }
        bind(HmacValidator.class).to(HmacValidator.class);
        bind(HmacDigest.class).to(HmacDigest.class);
        bind(new HmacDigest.HmacSha256MacFactory()).to(HmacDigest.HmacSha256MacFactory.class);
        bind(SecureCookieKeyConfigurationKeyStore.class).to(SecureCookieKeyStore.class);
        bind(CookieFactory.class).to(CookieFactory.class);

        // other

        bind(new DefaultConfigurationFactoryFactory<IdpStubsConfiguration>()
                .create(IdpStubsConfiguration.class, environment.getValidator(), environment.getObjectMapper(), ""))
                .to(new GenericType<ConfigurationFactory<IdpStubsConfiguration>>() {});

    }

    private void registerMetadataHealthcheckAndRefresh(Environment environment, MetadataResolver metadataResolver, MetadataResolverConfiguration metadataResolverConfiguration, String name) {
        String expectedEntityId = metadataResolverConfiguration.getExpectedEntityId();
        MetadataHealthCheck metadataHealthCheck = new MetadataHealthCheck(metadataResolver, expectedEntityId);
        environment.healthChecks().register(name, metadataHealthCheck);

        environment.admin().addTask(new Task(name + "-refresh") {
            @Override
            public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
                ((AbstractReloadingMetadataResolver) metadataResolver).refresh();
            }
        });
    }

    private IdaKeyStore getKeystoreFromConfig(SigningKeyPairConfiguration keyPairConfiguration) {
        PrivateKey privateSigningKey = keyPairConfiguration.getPrivateKey();
        X509Certificate signingCertificate = new X509CertificateFactory().createCertificate(keyPairConfiguration.getCert());
        PublicKey publicSigningKey = signingCertificate.getPublicKey();
        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);

        return new IdaKeyStore(signingCertificate, signingKeyPair, Collections.emptyList());
    }
}
