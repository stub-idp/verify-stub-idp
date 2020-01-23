package stubidp.stubidp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.jdbi.v3.core.Jdbi;
import stubidp.metrics.prometheus.bundle.PrometheusBundle;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.shared.csrf.CSRFCheckProtectionFeature;
import stubidp.shared.csrf.CSRFViewRenderer;
import stubidp.stubidp.auth.StubIdpBasicAuthRequiredFeature;
import stubidp.stubidp.bundles.DatabaseMigrationBundle;
import stubidp.stubidp.configuration.IdpStubsConfiguration;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.csrf.StubIDPCSRFCheckProtectionFilter;
import stubidp.stubidp.exceptions.mappers.CatchAllExceptionMapper;
import stubidp.stubidp.exceptions.mappers.FeatureNotEnabledExceptionMapper;
import stubidp.stubidp.exceptions.mappers.FileNotFoundExceptionMapper;
import stubidp.stubidp.exceptions.mappers.GenericStubIdpExceptionExceptionMapper;
import stubidp.stubidp.exceptions.mappers.IdpNotFoundExceptionMapper;
import stubidp.stubidp.exceptions.mappers.IdpUserNotFoundExceptionMapper;
import stubidp.stubidp.exceptions.mappers.InvalidAuthnRequestExceptionMapper;
import stubidp.stubidp.exceptions.mappers.InvalidEidasAuthnRequestExceptionMapper;
import stubidp.stubidp.exceptions.mappers.SessionSerializationExceptionMapper;
import stubidp.stubidp.exceptions.mappers.WebApplicationExceptionMapper;
import stubidp.stubidp.filters.NoCacheResponseFilter;
import stubidp.stubidp.filters.SecurityHeadersFilter;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASessionFeature;
import stubidp.stubidp.filters.StubIdpCacheControlFilter;
import stubidp.stubidp.healthcheck.DatabaseHealthCheck;
import stubidp.stubidp.healthcheck.StubIdpHealthCheck;
import stubidp.stubidp.listeners.StubIdpsFileListener;
import stubidp.stubidp.repositories.AllIdpsUserRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.repositories.jdbc.JDBIIdpSessionRepository;
import stubidp.stubidp.repositories.jdbc.JDBIUserRepository;
import stubidp.stubidp.repositories.jdbc.UserMapper;
import stubidp.stubidp.repositories.reaper.ManagedStaleSessionReaper;
import stubidp.stubidp.resources.eidas.EidasAuthnRequestReceiverResource;
import stubidp.stubidp.resources.eidas.EidasConsentResource;
import stubidp.stubidp.resources.eidas.EidasDebugPageResource;
import stubidp.stubidp.resources.eidas.EidasLoginPageResource;
import stubidp.stubidp.resources.eidas.EidasProxyNodeServiceMetadataResource;
import stubidp.stubidp.resources.eidas.EidasRegistrationPageResource;
import stubidp.stubidp.resources.idp.ConsentResource;
import stubidp.stubidp.resources.idp.DebugPageResource;
import stubidp.stubidp.resources.idp.GeneratePasswordResource;
import stubidp.stubidp.resources.idp.HeadlessIdpResource;
import stubidp.stubidp.resources.idp.IdpAuthnRequestReceiverResource;
import stubidp.stubidp.resources.idp.IdpMetadataResource;
import stubidp.stubidp.resources.idp.LoginPageResource;
import stubidp.stubidp.resources.idp.RegistrationPageResource;
import stubidp.stubidp.resources.idp.SecureLoginPageResource;
import stubidp.stubidp.resources.idp.SecureRegistrationPageResource;
import stubidp.stubidp.resources.idp.UserResource;
import stubidp.stubidp.resources.singleidp.SingleIdpHomePageResource;
import stubidp.stubidp.resources.singleidp.SingleIdpLogoutPageResource;
import stubidp.stubidp.resources.singleidp.SingleIdpPreRegistrationResource;
import stubidp.stubidp.resources.singleidp.SingleIdpStartPromptPageResource;
import stubidp.utils.rest.bundles.LoggingBundle;
import stubidp.utils.rest.bundles.MonitoringBundle;
import stubidp.utils.rest.bundles.ServiceStatusBundle;
import stubidp.utils.rest.filters.AcceptLanguageFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class StubIdpApplication extends Application<StubIdpConfiguration> {

    private MetadataResolverBundle<StubIdpConfiguration> idpMetadataResolverBundle;
    private MetadataResolverBundle<StubIdpConfiguration> eidasMetadataResolverBundle;

    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                String configFile = System.getenv("CONFIG_FILE");

                if (configFile == null) {
                    throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
                }

                new StubIdpApplication().run("server", configFile);
            } else {
                new StubIdpApplication().run(args);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "Stub Idp Service";
    }

    @Override
    public final void initialize(Bootstrap<StubIdpConfiguration> bootstrap) {

        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new PrometheusBundle());

        bootstrap.addBundle(new DatabaseMigrationBundle());

        bootstrap.addBundle(new ServiceStatusBundle<>());
        bootstrap.addBundle(new ViewBundle<>(singletonList(new CSRFViewRenderer())));
        bootstrap.addBundle(new LoggingBundle<>());
        bootstrap.addBundle(new MonitoringBundle());

        bootstrap.addBundle(new AssetsBundle("/assets/", "/assets/"));

        idpMetadataResolverBundle = new MetadataResolverBundle<>(x -> Optional.ofNullable(x.getMetadataConfiguration()));
        eidasMetadataResolverBundle = new MetadataResolverBundle<>(x -> Optional.ofNullable(x.getEuropeanIdentityConfiguration().getMetadata()));
        bootstrap.addBundle(idpMetadataResolverBundle);
        bootstrap.addBundle(eidasMetadataResolverBundle);

        bootstrap.getObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public final void run(StubIdpConfiguration configuration, Environment environment) {
        IdaSamlBootstrap.bootstrap();
        environment.servlets().addFilter("Cache Control", new StubIdpCacheControlFilter(configuration)).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/consent"+Urls.ROUTE_SUFFIX);
        environment.servlets().addFilter("Remove Accept-Language headers", AcceptLanguageFilter.class).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        environment.jersey().register(StubIdpBasicAuthRequiredFeature.class);
        environment.jersey().register(SessionCookieValueMustExistAsASessionFeature.class);
        environment.jersey().register(new CSRFCheckProtectionFeature(StubIDPCSRFCheckProtectionFilter.class));

        environment.getObjectMapper().setDateFormat(new StdDateFormat().withLocale(Locale.UK));

        environment.jersey().register(new StubIdpBinder(configuration, environment));
        environment.jersey().register(new StubIdpIdpBinder(configuration, environment, idpMetadataResolverBundle));
        environment.jersey().register(new StubIdpEidasBinder(configuration, environment, eidasMetadataResolverBundle));
        environment.jersey().register(new StubIdpSingleIdpBinder(configuration, environment));

        initialiseManaged(configuration, environment);

        // idp resources
        if(configuration.isIdpEnabled()) {
            environment.jersey().register(IdpAuthnRequestReceiverResource.class);
            environment.jersey().register(DebugPageResource.class);
            environment.jersey().register(ConsentResource.class);
            environment.jersey().register(IdpMetadataResource.class);

            // other idp resources
            environment.jersey().register(UserResource.class);
            environment.jersey().register(GeneratePasswordResource.class);

            // single idp resources
            if (configuration.getSingleIdpJourneyConfiguration().isEnabled()) {
                environment.jersey().register(SingleIdpStartPromptPageResource.class);
                environment.jersey().register(SingleIdpLogoutPageResource.class);
                environment.jersey().register(SingleIdpHomePageResource.class);
                environment.jersey().register(SingleIdpPreRegistrationResource.class);

                environment.jersey().register(LoginPageResource.class);
                environment.jersey().register(RegistrationPageResource.class);
            } else {
                environment.jersey().register(SecureLoginPageResource.class);
                environment.jersey().register(SecureRegistrationPageResource.class);
            }
        }

        // proxy node resources
        if(configuration.getEuropeanIdentityConfiguration().isEnabled()) {
            environment.jersey().register(EidasAuthnRequestReceiverResource.class);
            environment.jersey().register(EidasLoginPageResource.class);
            environment.jersey().register(EidasConsentResource.class);
            environment.jersey().register(EidasRegistrationPageResource.class);
            environment.jersey().register(EidasProxyNodeServiceMetadataResource.class);
            environment.jersey().register(EidasDebugPageResource.class);
        }

        // headless
        if(configuration.isHeadlessIdpEnabled()) {
            environment.jersey().register(HeadlessIdpResource.class);
        }

        //exception mappers
        environment.jersey().register(IdpNotFoundExceptionMapper.class);
        environment.jersey().register(IdpUserNotFoundExceptionMapper.class);
        environment.jersey().register(FileNotFoundExceptionMapper.class);
        environment.jersey().register(SessionSerializationExceptionMapper.class);
        environment.jersey().register(FeatureNotEnabledExceptionMapper.class);
        environment.jersey().register(GenericStubIdpExceptionExceptionMapper.class);
        environment.jersey().register(CatchAllExceptionMapper.class);
        environment.jersey().register(InvalidAuthnRequestExceptionMapper.class);
        environment.jersey().register(InvalidEidasAuthnRequestExceptionMapper.class);
        environment.jersey().register(WebApplicationExceptionMapper.class);

        //filters
        environment.jersey().register(NoCacheResponseFilter.class);
        environment.jersey().register(SecurityHeadersFilter.class);

        //health checks
        StubIdpHealthCheck healthCheck = new StubIdpHealthCheck();
        environment.healthChecks().register(healthCheck.getName(), healthCheck);

        DatabaseHealthCheck dbHealthCheck = new DatabaseHealthCheck(configuration.getDatabaseConfiguration().getUrl());
        environment.healthChecks().register("database", dbHealthCheck);
    }

    /**
     * This is working around Dropwizard Managed objects not being injectable via HK2
     */
    private void initialiseManaged(StubIdpConfiguration configuration, Environment environment) {
        final Jdbi jdbi = Jdbi.create(configuration.getDatabaseConfiguration().getUrl());
        if(configuration.isDynamicReloadOfStubIdpYmlEnabled()) {
            // This adds the hardcoded users into the db, which is done again during initialisation
            // Since the purpose is reloading this isn't so bad if this feature is enabled
            final AllIdpsUserRepository allIdpsUserRepository = new AllIdpsUserRepository(new JDBIUserRepository(jdbi, new UserMapper(Jackson.newObjectMapper()), false));
            final ConfigurationFactory<IdpStubsConfiguration> configurationFactory = new DefaultConfigurationFactoryFactory<IdpStubsConfiguration>()
                    .create(IdpStubsConfiguration.class, environment.getValidator(), environment.getObjectMapper(), "");
            final IdpStubsRepository idpStubsRepository = new IdpStubsRepository(allIdpsUserRepository, configuration, configurationFactory);
            environment.lifecycle().manage(new StubIdpsFileListener(configuration, idpStubsRepository));
        }
        environment.lifecycle().manage(new ManagedStaleSessionReaper(configuration, new JDBIIdpSessionRepository(jdbi, false)));
    }
}
