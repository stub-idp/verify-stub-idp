package stubidp.utils.rest.bundles;

import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import stubidp.utils.rest.configuration.ServiceStatus;
import stubidp.utils.rest.filters.ConnectionCloseFilter;
import stubidp.utils.rest.resources.ServiceStatusResource;
import stubidp.utils.rest.tasks.SetServiceUnavailableTask;

public class ServiceStatusBundle<T extends Configuration> implements ConfiguredBundle<T> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        environment.jersey().register(new ServiceStatusResource());
        environment.jersey().register(new ConnectionCloseFilter());
        environment.admin().addTask(new SetServiceUnavailableTask(ServiceStatus.getInstance()));
    }
}
