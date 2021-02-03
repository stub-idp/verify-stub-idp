package uk.gov.ida.matchingserviceadapter;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.utils.rest.bundles.LoggingBundle;
import stubidp.utils.rest.bundles.MonitoringBundle;
import stubidp.utils.rest.bundles.ServiceStatusBundle;
import uk.gov.ida.matchingserviceadapter.exceptions.ExceptionExceptionMapper;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlOverSoapExceptionMapper;
import uk.gov.ida.matchingserviceadapter.healthcheck.MatchingServiceAdapterHealthCheck;
import uk.gov.ida.matchingserviceadapter.resources.LocalMetadataResource;
import uk.gov.ida.matchingserviceadapter.resources.MatchingServiceResource;
import uk.gov.ida.matchingserviceadapter.resources.UnknownUserAttributeQueryResource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterBinder.registerMetadataRefreshTask;

public class MatchingServiceAdapterApplication extends Application<MatchingServiceAdapterConfiguration> {

    private MetadataResolverBundle<MatchingServiceAdapterConfiguration> metadataResolverBundle;

    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                String configFile = System.getenv("CONFIG_FILE");

                if (configFile == null) {
                    throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
                }

                new MatchingServiceAdapterApplication().run("server", configFile);
            } else {
                new MatchingServiceAdapterApplication().run(args);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "Matching Service Adapter";
    }

    @Override
    public final void initialize(Bootstrap<MatchingServiceAdapterConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        metadataResolverBundle = new MetadataResolverBundle<>(MatchingServiceAdapterConfiguration::getMetadataConfiguration);
        bootstrap.addBundle(metadataResolverBundle);
        bootstrap.addBundle(new LoggingBundle<>());
        bootstrap.addBundle(new MonitoringBundle());
        bootstrap.addBundle(new ServiceStatusBundle<>());
    }

    @Override
    public final void run(MatchingServiceAdapterConfiguration configuration, Environment environment) {
        IdaSamlBootstrap.bootstrap();

        MatchingServiceAdapterBinder matchingServiceAdapterBinder = new MatchingServiceAdapterBinder(configuration, environment, metadataResolverBundle);
        environment.jersey().register(matchingServiceAdapterBinder);

        environment.getObjectMapper().setDateFormat(StdDateFormat.getDateInstance());

        environment.jersey().register(LocalMetadataResource.class);
        environment.jersey().register(MatchingServiceResource.class);
        environment.jersey().register(UnknownUserAttributeQueryResource.class);

        environment.jersey().register(SamlOverSoapExceptionMapper.class);
        environment.jersey().register(ExceptionExceptionMapper.class);

        registerMetadataRefreshTask(environment, Optional.empty(), Collections.singletonList(metadataResolverBundle.getMetadataResolver()), "metadata");

        MatchingServiceAdapterHealthCheck healthCheck = new MatchingServiceAdapterHealthCheck();
        environment.healthChecks().register(healthCheck.getName(), healthCheck);
    }

}
