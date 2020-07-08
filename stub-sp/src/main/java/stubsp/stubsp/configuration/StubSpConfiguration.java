package stubsp.stubsp.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.util.Duration;
import stubidp.metrics.prometheus.config.PrometheusConfiguration;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import stubidp.saml.metadata.MultiTrustStoresBackedMetadataConfiguration;
import stubidp.shared.configuration.SigningKeyPairConfiguration;
import stubidp.utils.rest.cache.AssetCacheConfiguration;
import stubidp.utils.rest.common.ServiceInfoConfiguration;
import stubidp.utils.rest.configuration.ServiceNameConfiguration;
import stubidp.utils.security.configuration.SecureCookieConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

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
    private String assetsCacheDuration = Duration.days(1).toString();

    @Valid
    @NotNull
    @JsonProperty
    private SecureCookieConfiguration secureCookieConfiguration;

    @Valid
    @NotNull
    @JsonProperty
    protected Duration assertionLifetime;

    @JsonProperty
    @NotNull
    @Valid
    protected ServiceInfoConfiguration serviceInfo;

    @NotNull
    @Valid
    @JsonProperty
    protected SamlConfigurationImpl saml;

    @NotNull
    @Valid
    @JsonProperty
    protected SigningKeyPairConfiguration signingKeyPairConfiguration;

    @NotNull
    @Valid
    @JsonProperty
    protected SigningKeyPairConfiguration encryptionKeyPairConfiguration;

    @NotNull
    @Valid
    @JsonProperty
    protected SigningKeyPairConfiguration spMetadataSigningKeyPairConfiguration;

    @NotNull
    @Valid
    @JsonProperty
    protected MultiTrustStoresBackedMetadataConfiguration metadata;

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

    public boolean isCacheAssets() {
        return cacheAssets;
    }

    public Duration getAssertionLifetime() {
        return assertionLifetime;
    }

    public ServiceInfoConfiguration getServiceInfo() {
        return serviceInfo;
    }

    public SamlConfigurationImpl getSaml() {
        return saml;
    }

    public SigningKeyPairConfiguration getSigningKeyPairConfiguration() {
        return signingKeyPairConfiguration;
    }

    public SigningKeyPairConfiguration getEncryptionKeyPairConfiguration() {
        return encryptionKeyPairConfiguration;
    }

    public SigningKeyPairConfiguration getSpMetadataSigningKeyPairConfiguration() {
        return spMetadataSigningKeyPairConfiguration;
    }

    public Optional<MetadataResolverConfiguration> getMetadata() {
        return Optional.ofNullable(metadata);
    }

}
