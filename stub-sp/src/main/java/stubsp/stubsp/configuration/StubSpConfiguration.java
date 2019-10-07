package stubsp.stubsp.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import stubidp.metrics.prometheus.config.PrometheusConfiguration;
import stubidp.utils.rest.configuration.ServiceNameConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class StubSpConfiguration extends Configuration implements PrometheusConfiguration, ServiceNameConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private boolean prometheusEnabled = true;

    @Valid
    @NotNull
    @JsonProperty
    private String serviceName = "Stub Sp Service";

    @Override
    public boolean isPrometheusEnabled() {
        return prometheusEnabled;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }
}
