package stubidp.utils.rest.bundles;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import stubidp.utils.rest.configuration.ServiceNameConfiguration;
import stubidp.utils.rest.resources.ServiceNameResource;
import stubidp.utils.rest.resources.VersionInfoResource;

public class MonitoringBundle implements ConfiguredBundle<ServiceNameConfiguration> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(ServiceNameConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new ServiceNameResource(configuration.getServiceName()));
        environment.jersey().register(new VersionInfoResource());
    }
}
