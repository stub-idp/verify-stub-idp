package stubsp.stubsp;

import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.metadata.MetadataConfiguration;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.stubidp.configuration.SamlConfiguration;
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
import stubsp.stubsp.services.AvailableServicesService;
import stubsp.stubsp.services.InitiateSingleIdpJourneyService;
import stubsp.stubsp.services.RootService;
import stubsp.stubsp.services.SamlResponseService;
import stubsp.stubsp.services.SamlSpMetadataService;
import stubsp.stubsp.services.SecureService;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.IS_SECURE_COOKIE_ENABLED;

public class StubSpBinder extends AbstractBinder {

    public static final String SERVICE_NAME = "serviceName";
    public static final String SP_METADATA_REPOSITORY = "SpMetadataRepository";
    public static final String SP_METADATA_SIGNATURE_FACTORY = "SpMetadataSignatureFactory";
    public static final String SP_SIGNING_CERT = "SpSigningCert";
    public static final String METADATA_VALIDITY_PERIOD = "metadataValidityPeriod";
    private static final String SP_METADATA_RESOLVER = "SpMetadataResolver";
    private static final String SP_METADATA_CONFIGURATION = "SpMetadataConfiguration";

    private final StubSpConfiguration configuration;
    private final Environment environment;
    private final MetadataResolverBundle<StubSpConfiguration> metadataResolverBundle;

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

        // saml
        bind(SamlMessageRedirectViewFactory.class).to(SamlMessageRedirectViewFactory.class);
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

//        final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        bind(configuration.getMetadata()).named(SP_METADATA_CONFIGURATION).to(MetadataConfiguration.class);
        bind(SpMetadataBuilder.class).to(SpMetadataBuilder.class);
        bind(new Period().withDays(1)).named(METADATA_VALIDITY_PERIOD).to(ReadablePeriod.class);

        final SignatureRSASHA256 signatureAlgorithm = new SignatureRSASHA256();
        bind(signatureAlgorithm).to(SignatureAlgorithm.class);
        final DigestSHA256 digestAlgorithm = new DigestSHA256();
        bind(digestAlgorithm).to(DigestAlgorithm.class);

        final IdaKeyStore spMetadataSigningKeyStore = getKeystoreFromConfig(configuration.getSpMetadataSigningKeyPairConfiguration());
//        bind(spMetadataSigningKeyStore).named(SP_METADATA_SIGNING_KEYSTORE).to(IdaKeyStore.class);
        final SignatureFactory signatureFactory = new SignatureFactory(true, new IdaKeyStoreCredentialRetriever(spMetadataSigningKeyStore), signatureAlgorithm, digestAlgorithm);
        bind(signatureFactory).named(SP_METADATA_SIGNATURE_FACTORY).to(SignatureFactory.class);
        bind(configuration.getSigningKeyPairConfiguration().getCert()).named(SP_SIGNING_CERT).to(String.class);
    }

    private IdaKeyStore getKeystoreFromConfig(SigningKeyPairConfiguration keyPairConfiguration) {
        PrivateKey privateSigningKey = keyPairConfiguration.getPrivateKey();
        X509Certificate signingCertificate = new X509CertificateFactory().createCertificate(keyPairConfiguration.getCert());
        PublicKey publicSigningKey = signingCertificate.getPublicKey();
        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);

        return new IdaKeyStore(signingCertificate, signingKeyPair, Collections.emptyList());
    }
}