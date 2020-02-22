package stubidp.dropwizard.logstash.support;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import stubidp.dropwizard.logstash.LogstashBundle;

public class TestApplication extends Application<TestConfiguration> {
    @Override
    public void initialize(Bootstrap<TestConfiguration> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.addBundle(new LogstashBundle<>());
    }

    @Override
    public void run(TestConfiguration configuration, Environment environment) {
        environment.jersey().register(new RootResource());
    }
}
