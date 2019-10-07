package stubsp.stubsp;

import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import stubsp.stubsp.configuration.StubSpConfiguration;
import stubsp.stubsp.services.AvailableServicesService;
import stubsp.stubsp.services.InitiateSingleIdpJourneyService;
import stubsp.stubsp.services.RootService;
import stubsp.stubsp.services.SamlSpMetadataService;
import stubsp.stubsp.services.SamlResponseService;
import stubsp.stubsp.services.SecureService;

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
        bind(configuration).to(StubSpConfiguration.class);

        // services
        bind(RootService.class).to(RootService.class);
        bind(AvailableServicesService.class).to(AvailableServicesService.class);
        bind(SecureService.class).to(SecureService.class);
        bind(SamlResponseService.class).to(SamlResponseService.class);
        bind(SamlSpMetadataService.class).to(SamlSpMetadataService.class);
        bind(InitiateSingleIdpJourneyService.class).to(InitiateSingleIdpJourneyService.class);
    }
}