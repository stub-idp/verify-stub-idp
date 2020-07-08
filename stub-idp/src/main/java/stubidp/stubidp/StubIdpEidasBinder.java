package stubidp.stubidp;

import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.security.signature.SignatureRSASSAPSS;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.shared.configuration.SigningKeyPairConfiguration;
import stubidp.shared.repositories.MetadataRepository;
import stubidp.stubidp.builders.CountryMetadataBuilder;
import stubidp.stubidp.builders.CountryMetadataSigningHelper;
import stubidp.stubidp.configuration.EuropeanIdentityConfiguration;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.stubidp.repositories.jdbc.JDBIEidasSessionRepository;
import stubidp.stubidp.saml.EidasAuthnRequestValidator;
import stubidp.stubidp.saml.locators.IdpHardCodedEntityToEncryptForLocator;
import stubidp.stubidp.saml.transformers.outbound.EidasResponseTransformerProvider;
import stubidp.stubidp.security.HubEncryptionKeyStore;
import stubidp.stubidp.services.EidasAuthnResponseService;
import stubidp.stubidp.services.StubCountryService;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.GenericType;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Optional;

public class StubIdpEidasBinder extends AbstractBinder {

    public static final String HUB_CONNECTOR_METADATA_RESOLVER = "HubConnectorMetadataResolver";
    public static final String HUB_CONNECTOR_METADATA_REPOSITORY = "HubConnectorMetadataRepository";
    public static final String HUB_CONNECTOR_ENCRYPTION_KEY_STORE = "HubConnectorEncryptionKeyStore";
    public static final String HUB_CONNECTOR_ENTITY_ID = "HubConnectorEntityId";
    public static final String STUB_COUNTRY_METADATA_URL = "StubCountryMetadataUrl";
    public static final String STUB_COUNTRY_SSO_URL = "StubCountrySsoUrl";
    public static final String COUNTRY_SIGNING_KEY_STORE = "CountrySigningKeyStore";
    public static final String COUNTRY_METADATA_SIGNATURE_FACTORY = "countryMetadataSignatureFactory";
    public static final String RSASHA256_EIDAS_AUTHN_RESPONSE_SERVICE = "RSASHA256EidasAuthnResponseService";
    public static final String RSASSAPSS_EIDAS_AUTHN_RESPONSE_SERVICE = "RSASSAPSSEidasAuthnResponseService";
    private final String RSASHA256_EIDAS_RESPONSE_TRANSFORMER_PROVIDER = "RSASHA256EidasResponseTransfomerProvider";
    private final String RSASSAPSS_EIDAS_RESPONSE_TRANSFORMER_PROVIDER = "RSASSAPSSEidasResponseTransformerProvider";

    private final StubIdpConfiguration stubIdpConfiguration;
    private final Environment environment;
    private final MetadataResolverBundle<StubIdpConfiguration> eidasMetadataResolverBundle;

    StubIdpEidasBinder(StubIdpConfiguration stubIdpConfiguration,
                       Environment environment,
                       MetadataResolverBundle<StubIdpConfiguration> eidasMetadataResolverBundle) {
        this.stubIdpConfiguration = stubIdpConfiguration;
        this.environment = environment;
        this.eidasMetadataResolverBundle = eidasMetadataResolverBundle;
    }

    @Override
    protected void configure() {

        bind(JDBIEidasSessionRepository.class).in(Singleton.class).to(EidasSessionRepository.class);

        if (stubIdpConfiguration.getEuropeanIdentityConfiguration().isEnabled()) {
            bind(StubCountryRepository.class).in(Singleton.class).to(StubCountryRepository.class);

            bind(stubIdpConfiguration.getEuropeanIdentityConfiguration()).to(EuropeanIdentityConfiguration.class);

            final String hubConnectorEntityId = stubIdpConfiguration.getEuropeanIdentityConfiguration().getHubConnectorEntityId();
            bind(hubConnectorEntityId).named(HUB_CONNECTOR_ENTITY_ID).to(String.class);
            final String stubCountryMetadataUrl = stubIdpConfiguration.getEuropeanIdentityConfiguration().getStubCountryBaseUrl() + Urls.EIDAS_METADATA_RESOURCE;
            bind(stubCountryMetadataUrl).named(STUB_COUNTRY_METADATA_URL).to(String.class);
            final String stubCountrySsoUrl = stubIdpConfiguration.getEuropeanIdentityConfiguration().getStubCountryBaseUrl() + Urls.EIDAS_SAML2_SSO_RESOURCE;
            bind(stubCountrySsoUrl).named(STUB_COUNTRY_SSO_URL).to(String.class);

            final SignatureRSASHA256 signatureAlgorithm = new SignatureRSASHA256();
            bind(signatureAlgorithm).to(SignatureAlgorithm.class);
            final DigestSHA256 digestAlgorithm = new DigestSHA256();
            bind(digestAlgorithm).to(DigestAlgorithm.class);

            final IdaKeyStore countryKeyStore = getKeystoreFromConfig(stubIdpConfiguration.getEuropeanIdentityConfiguration().getSigningKeyPairConfiguration());
            bind(countryKeyStore).named(COUNTRY_SIGNING_KEY_STORE).to(IdaKeyStore.class);
            final SignatureFactory signatureFactory = new SignatureFactory(true, new IdaKeyStoreCredentialRetriever(countryKeyStore), signatureAlgorithm, digestAlgorithm);
            bind(signatureFactory).named(COUNTRY_METADATA_SIGNATURE_FACTORY).to(SignatureFactory.class);

            final MetadataResolver metadataResolver = eidasMetadataResolverBundle.getMetadataResolver();
            bind(metadataResolver).named(HUB_CONNECTOR_METADATA_RESOLVER).to(MetadataResolver.class);
            final MetadataRepository eidasMetadataRepository = new MetadataRepository(eidasMetadataResolverBundle.getMetadataCredentialResolver(), hubConnectorEntityId);
            bind(eidasMetadataRepository).named(HUB_CONNECTOR_METADATA_REPOSITORY).to(MetadataRepository.class);
            final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
            final Optional<HubEncryptionKeyStore> eidasHubEncryptionKeyStore = Optional.of(new HubEncryptionKeyStore(eidasMetadataRepository, publicKeyFactory));
            bind(eidasHubEncryptionKeyStore).named(HUB_CONNECTOR_ENCRYPTION_KEY_STORE).to(new GenericType<Optional<EncryptionKeyStore>>() {});

            final EntityToEncryptForLocator entityToEncryptForLocator = new IdpHardCodedEntityToEncryptForLocator(hubConnectorEntityId);
            bind(entityToEncryptForLocator).to(EntityToEncryptForLocator.class);

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

            bind(EidasAuthnRequestValidator.class).to(EidasAuthnRequestValidator.class);
        }
    }

    private IdaKeyStore getKeystoreFromConfig(SigningKeyPairConfiguration keyPairConfiguration) {
        PrivateKey privateSigningKey = keyPairConfiguration.getPrivateKey();
        X509Certificate signingCertificate = new X509CertificateFactory().createCertificate(keyPairConfiguration.getCert());
        PublicKey publicSigningKey = signingCertificate.getPublicKey();
        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);

        return new IdaKeyStore(signingCertificate, signingKeyPair, Collections.emptyList());
    }
}
