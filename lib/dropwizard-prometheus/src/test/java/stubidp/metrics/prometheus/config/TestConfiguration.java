package stubidp.metrics.prometheus.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import stubidp.metrics.prometheus.config.PrometheusConfiguration;

import javax.validation.constraints.NotNull;

public class TestConfiguration extends Configuration implements PrometheusConfiguration {

    @JsonProperty
    @NotNull
    private boolean prometheusEnabled = true;

    public TestConfiguration() {
    }

    @Override
    public boolean isPrometheusEnabled() {
        return prometheusEnabled;
    }
}
