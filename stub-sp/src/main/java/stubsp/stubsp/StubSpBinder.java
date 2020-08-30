package stubsp.stubsp;

import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.domain.configuration.SamlConfiguration;
import stubidp.saml.metadata.MetadataConfiguration;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.shared.configuration.SigningKeyPairConfiguration;
import stubidp.shared.cookies.CookieNames;
import stubidp.shared.cookies.HmacValidator;
import stubidp.shared.repositories.MetadataRepository;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.utils.security.configuration.SecureCookieConfiguration;
import stubidp.utils.security.configuration.SecureCookieKeyStore;
import stubidp.utils.security.security.HmacDigest;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.SecureCookieKeyConfigurationKeyStore;
import stubidp.utils.security.security.X509CertificateFactory;
import stubsp.stubsp.builders.SpMetadataBuilder;
import stubsp.stubsp.configuration.StubSpConfiguration;
import stubsp.stubsp.cookies.StubSpCookieNames;
import stubsp.stubsp.saml.locators.StubIdpEntityToEncryptForLocator;
import stubsp.stubsp.saml.response.SamlResponseDecrypter;
import stubsp.stubsp.services.AvailableServicesService;
import stubsp.stubsp.services.InitiateSingleIdpJourneyService;
import stubsp.stubsp.services.RootService;
import stubsp.stubsp.services.SamlResponseService;
import stubsp.stubsp.services.SamlSpMetadataService;
import stubsp.stubsp.services.SecureService;

import javax.ws.rs.core.GenericType;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.IS_SECURE_COOKIE_ENABLED;

public class StubSpBinder extends AbstractBinder {

    public static final String SERVICE_NAME = "serviceName";
    public static final String SP_METADATA_REPOSITORY = "SpMetadataRepository";
    public static final String SP_METADATA_SIGNATURE_FACTORY = "SpMetadataSignatureFactory";
    public static final String SP_SIGNING_CERT = "SpSigningCert";
    public static final String SP_ENCRYPTION_CERT = "SpEncryptionCert";
    public static final String METADATA_VALIDITY_PERIOD = "metadataValidityPeriod";
    public static final String SP_METADATA_RESOLVER = "SpMetadataResolver";
    public static final String SP_CHECK_KEY_INFO = "SpCheckKeyInfo";
    public static final String SP_KEY_STORE = "SpKeyStore";
    private static final String SP_METADATA_CONFIGURATION = "SpMetadataConfiguration";

    public static final String EIDAS_KEY_STORE = "EidasKeyStore";
    public static final String EIDAS_METADATA_RESOLVER = "EidasMetadataResolver";

    private final StubSpConfiguration configuration;
    private final Environment environment;
    private final MetadataResolverBundle<StubSpConfiguration> metadataResolverBundle;
    private final X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();

    public StubSpBinder(StubSpConfiguration configuration,
                        Environment environment,
                        MetadataResolverBundle<StubSpConfiguration> metadataResolverBundle) {
        this.configuration = configuration;
        this.environment = environment;
        this.metadataResolverBundle = metadataResolverBundle;
    }

    @Override
    protected void configure() {
        // configuration
        bind(configuration.getServiceName()).named(SERVICE_NAME).to(String.class);
        bind(configuration.getSecureCookieConfiguration().isSecure()).named(IS_SECURE_COOKIE_ENABLED).to(Boolean.class);
        bind(configuration.getSecureCookieConfiguration()).to(SecureCookieConfiguration.class);
        bind(configuration).to(StubSpConfiguration.class);
        bind(SecureCookieKeyConfigurationKeyStore.class).to(SecureCookieKeyStore.class);

        // services
        bind(RootService.class).to(RootService.class);
        bind(AvailableServicesService.class).to(AvailableServicesService.class);
        bind(SecureService.class).to(SecureService.class);
        bind(SamlResponseService.class).to(SamlResponseService.class);
        bind(SamlSpMetadataService.class).to(SamlSpMetadataService.class);
        bind(InitiateSingleIdpJourneyService.class).to(InitiateSingleIdpJourneyService.class);

        // security
        bind(StubSpCookieNames.class).to(CookieNames.class);

        // saml response processing
        bind(Boolean.FALSE).named(SP_CHECK_KEY_INFO).to(Boolean.class);
        bind(Optional.empty()).named(EIDAS_METADATA_RESOLVER).to(new GenericType<Optional<MetadataResolver>>() {});
        bind(Optional.empty()).named(EIDAS_KEY_STORE).to(new GenericType<Optional<IdaKeyStore>>() {});
        bind(getKeystoreFromConfig(Optional.empty(), Optional.of(configuration.getEncryptionKeyPairConfiguration()))).named(SP_KEY_STORE).to(IdaKeyStore.class);

        // saml
        bind(SamlMessageRedirectViewFactory.class).to(SamlMessageRedirectViewFactory.class);
        bind(SamlResponseDecrypter.class).to(SamlResponseDecrypter.class);
        bind(HmacValidator.class).to(HmacValidator.class);
        bind(HmacDigest.class).to(HmacDigest.class);
        bind(HmacDigest.HmacSha256MacFactory.class).to(HmacDigest.HmacSha256MacFactory.class);
        bind(configuration.getSaml()).to(SamlConfiguration.class);
        final StubIdpEntityToEncryptForLocator entityToEncryptForLocator = new StubIdpEntityToEncryptForLocator(configuration.getMetadata().get().getExpectedEntityId());
        bind(entityToEncryptForLocator).to(EntityToEncryptForLocator.class);
        bind(PublicKeyFactory.class).to(PublicKeyFactory.class);

        bind(metadataResolverBundle.getMetadataResolver()).named(SP_METADATA_RESOLVER).to(MetadataResolver.class);
        final MetadataRepository idpMetadataRepository = new MetadataRepository(metadataResolverBundle.getMetadataCredentialResolver(), configuration.getMetadata().get().getExpectedEntityId());
        bind(idpMetadataRepository).named(SP_METADATA_REPOSITORY).to(MetadataRepository.class);

        bind(configuration.getMetadata()).named(SP_METADATA_CONFIGURATION).to(MetadataConfiguration.class);
        bind(SpMetadataBuilder.class).to(SpMetadataBuilder.class);
        bind(Duration.ofDays(1)).named(METADATA_VALIDITY_PERIOD).to(Duration.class);

        final SignatureRSASHA256 signatureAlgorithm = new SignatureRSASHA256();
        bind(signatureAlgorithm).to(SignatureAlgorithm.class);
        final DigestSHA256 digestAlgorithm = new DigestSHA256();
        bind(digestAlgorithm).to(DigestAlgorithm.class);

        final IdaKeyStore spMetadataSigningKeyStore = getKeystoreFromConfig(Optional.of(configuration.getSpMetadataSigningKeyPairConfiguration()), Optional.empty());
        final SignatureFactory signatureFactory = new SignatureFactory(true, new IdaKeyStoreCredentialRetriever(spMetadataSigningKeyStore), signatureAlgorithm, digestAlgorithm);
        bind(signatureFactory).named(SP_METADATA_SIGNATURE_FACTORY).to(SignatureFactory.class);
        bind(configuration.getSigningKeyPairConfiguration().getCert()).named(SP_SIGNING_CERT).to(String.class);
        bind(configuration.getEncryptionKeyPairConfiguration().getCert()).named(SP_ENCRYPTION_CERT).to(String.class);
    }

    private IdaKeyStore getKeystoreFromConfig(Optional<SigningKeyPairConfiguration> signingKeyPairConfiguration, Optional<SigningKeyPairConfiguration> encryptionKeyPairConfiguration) {
        X509Certificate signingCertificate = null;
        KeyPair signingKeyPair = null;
        if(signingKeyPairConfiguration.isPresent()) {
            PrivateKey privateSigningKey = signingKeyPairConfiguration.get().getPrivateKey();
            signingCertificate = x509CertificateFactory.createCertificate(signingKeyPairConfiguration.get().getCert());
            PublicKey publicSigningKey = signingCertificate.getPublicKey();
            signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
        }
        List<KeyPair> encryptionKeyPairs = new ArrayList<>();
        encryptionKeyPairConfiguration.ifPresent(k -> encryptionKeyPairs.add(new KeyPair(x509CertificateFactory.createCertificate(k.getCert()).getPublicKey(), k.getPrivateKey())));

        return new IdaKeyStore(signingCertificate, signingKeyPair, encryptionKeyPairs);
    }
}