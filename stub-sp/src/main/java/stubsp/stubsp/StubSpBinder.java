package stubsp.stubsp;

import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import stubidp.saml.metadata.MetadataConfiguration;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.stubidp.configuration.SamlConfiguration;
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
import stubsp.stubsp.configuration.StubSpConfiguration;
import stubsp.stubsp.cookies.StubSpCookieNames;
import stubsp.stubsp.saml.locators.StubIdpEntityToEncryptForLocator;
import stubsp.stubsp.services.AvailableServicesService;
import stubsp.stubsp.services.InitiateSingleIdpJourneyService;
import stubsp.stubsp.services.RootService;
import stubsp.stubsp.services.SamlResponseService;
import stubsp.stubsp.services.SamlSpMetadataService;
import stubsp.stubsp.services.SecureService;

import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.IS_SECURE_COOKIE_ENABLED;

public class StubSpBinder extends AbstractBinder {

    public static final String SERVICE_NAME = "serviceName";
    private static final String IDP_METADATA_RESOLVER = "IdpMetadataResolver";
    private static final String IDP_METADATA_REPOSITORY = "IdpMetadataRepository";
    private static final String IDP_METADATA_CONFIGURATION = "IdpMetadataConfiguration";

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
        bind(metadataResolverBundle.getMetadataResolver()).named(IDP_METADATA_RESOLVER).to(MetadataResolver.class);
        final MetadataRepository idpMetadataRepository = new MetadataRepository(metadataResolverBundle.getMetadataCredentialResolver(), configuration.getMetadata().get().getExpectedEntityId());
        bind(idpMetadataRepository).named(IDP_METADATA_REPOSITORY).to(MetadataRepository.class);
        final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        bind(configuration.getMetadata()).named(IDP_METADATA_CONFIGURATION).to(MetadataConfiguration.class);
    }

}