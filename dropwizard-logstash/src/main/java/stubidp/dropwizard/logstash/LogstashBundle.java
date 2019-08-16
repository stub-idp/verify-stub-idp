package stubidp.dropwizard.logstash;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;

public class LogstashBundle<T> implements ConfiguredBundle<T> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().getSubtypeResolver().registerSubtypes(
                AccessLogstashConsoleAppenderFactory.class,
                LogstashConsoleAppenderFactory.class,
                LogstashSyslogAppenderFactory.class,
                LogstashFileAppenderFactory.class
        );
    }
}
