package stubsp.stubsp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;
import stubidp.metrics.prometheus.bundle.PrometheusBundle;
import stubidp.utils.rest.bundles.LoggingBundle;
import stubidp.utils.rest.bundles.MonitoringBundle;
import stubidp.utils.rest.bundles.ServiceStatusBundle;
import stubsp.stubsp.configuration.StubSpConfiguration;
import stubsp.stubsp.resources.AvailableServicesResource;
import stubsp.stubsp.resources.InitiateSingleIdpJourneyResource;
import stubsp.stubsp.resources.RootResource;
import stubsp.stubsp.resources.SamlSpMetadataResource;
import stubsp.stubsp.resources.SamlResponseResource;
import stubsp.stubsp.resources.SecureResource;

import java.util.Locale;

import static java.util.Collections.singletonList;

public class StubSpApplication extends Application<StubSpConfiguration> {

    public static void main(String[] args) throws Exception {
        new StubSpApplication().run(args);
    }

    @Override
    public String getName() {
        return "Stub Sp Service";
    }

    @Override
    public final void initialize(Bootstrap<StubSpConfiguration> bootstrap) {

        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new PrometheusBundle());
        bootstrap.addBundle(new ServiceStatusBundle<>());
        //TODO: use CSRFViewRenderer
        bootstrap.addBundle(new ViewBundle<>(singletonList(new FreemarkerViewRenderer())));
        bootstrap.addBundle(new LoggingBundle<>());
        bootstrap.addBundle(new MonitoringBundle());

        bootstrap.addBundle(new AssetsBundle("/assets/", "/assets/"));

        bootstrap.getObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public void run(StubSpConfiguration configuration, Environment environment) {
        environment.getObjectMapper().setDateFormat(new StdDateFormat().withLocale(Locale.UK));
        environment.jersey().register(new StubSpBinder(configuration, environment));

        environment.jersey().register(RootResource.class);
        environment.jersey().register(SecureResource.class);
        environment.jersey().register(AvailableServicesResource.class);
        environment.jersey().register(SamlResponseResource.class);
        environment.jersey().register(SamlSpMetadataResource.class);
        environment.jersey().register(InitiateSingleIdpJourneyResource.class);
    }
}