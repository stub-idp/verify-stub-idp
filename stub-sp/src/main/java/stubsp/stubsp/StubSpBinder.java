package stubsp.stubsp;

import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import stubidp.stubidp.cookies.CookieNames;
import stubidp.stubidp.cookies.HmacValidator;
import stubidp.stubidp.views.SamlMessageRedirectViewFactory;
import stubidp.utils.security.configuration.SecureCookieConfiguration;
import stubidp.utils.security.configuration.SecureCookieKeyStore;
import stubidp.utils.security.security.HmacDigest;
import stubidp.utils.security.security.SecureCookieKeyConfigurationKeyStore;
import stubsp.stubsp.configuration.StubSpConfiguration;
import stubsp.stubsp.cookies.StubSpCookieNames;
import stubsp.stubsp.services.AvailableServicesService;
import stubsp.stubsp.services.InitiateSingleIdpJourneyService;
import stubsp.stubsp.services.RootService;
import stubsp.stubsp.services.SamlResponseService;
import stubsp.stubsp.services.SamlSpMetadataService;
import stubsp.stubsp.services.SecureService;

import static stubidp.stubidp.csrf.AbstractCSRFCheckProtectionFilter.IS_SECURE_COOKIE_ENABLED;

public class StubSpBinder extends AbstractBinder {

    public static final String SERVICE_NAME = "serviceName";

    private final StubSpConfiguration configuration;
    private final Environment environment;

    public StubSpBinder(StubSpConfiguration configuration, Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
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

    }
}