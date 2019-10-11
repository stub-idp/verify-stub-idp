package stubsp.stubsp.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import stubidp.metrics.prometheus.config.PrometheusConfiguration;
import stubidp.utils.rest.cache.AssetCacheConfiguration;
import stubidp.utils.rest.configuration.ServiceNameConfiguration;
import stubidp.utils.security.configuration.KeyConfiguration;
import stubidp.utils.security.configuration.SecureCookieConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;

public class StubSpConfiguration extends Configuration implements PrometheusConfiguration,
        ServiceNameConfiguration,
        AssetCacheConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private boolean prometheusEnabled = true;

    @Valid
    @NotNull
    @JsonProperty
    private String serviceName = "Stub Sp Service";

    @Valid
    @NotNull
    @JsonProperty
    private boolean cacheAssets = false;

    @Valid
    @NotNull
    @JsonProperty
    private String assetsCacheDuration = Duration.ofDays(1).toString();

    @Valid
    @NotNull
    @JsonProperty
    private SecureCookieConfiguration secureCookieConfiguration = new SecureCookieConfiguration() {{ this.secure = false; this.keyConfiguration = new KeyConfiguration() {{}}; }};

    @Override
    public boolean isPrometheusEnabled() {
        return prometheusEnabled;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean shouldCacheAssets() {
        return cacheAssets;
    }

    @Override
    public String getAssetsCacheDuration() {
        return assetsCacheDuration;
    }

    public SecureCookieConfiguration getSecureCookieConfiguration() {
        return secureCookieConfiguration;
    }
}
