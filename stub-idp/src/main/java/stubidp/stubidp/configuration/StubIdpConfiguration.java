package stubidp.stubidp.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.util.Duration;
import stubidp.metrics.prometheus.config.PrometheusConfiguration;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import stubidp.saml.metadata.MultiTrustStoresBackedMetadataConfiguration;
import stubidp.saml.stubidp.configuration.SamlConfiguration;
import stubidp.stubidp.repositories.reaper.StaleSessionReaperConfiguration;
import stubidp.utils.rest.cache.AssetCacheConfiguration;
import stubidp.utils.rest.common.ServiceInfoConfiguration;
import stubidp.utils.rest.configuration.ServiceNameConfiguration;
import stubidp.utils.security.configuration.SecureCookieConfiguration;

import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Singleton
public class StubIdpConfiguration extends Configuration implements
        AssertionLifetimeConfiguration,
        AssetCacheConfiguration,
        ServiceNameConfiguration,
        PrometheusConfiguration {

    @Valid
    @JsonProperty
    private String assetsCacheDuration = "0";

    @Valid
    @JsonProperty
    private boolean shouldCacheAssets = false;

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

    @Valid
    @JsonProperty
    @NotNull
    protected Boolean basicAuthEnabledForUserResource = true;

    @NotNull
    @Valid
    @JsonProperty
    protected String stubIdpsYmlFileLocation;

    @NotNull
    @Valid
    @JsonProperty
    protected Duration stubIdpYmlFileRefresh;

    @NotNull
    @Valid
    @JsonProperty
    protected MultiTrustStoresBackedMetadataConfiguration metadata;

    @NotNull
    @Valid
    @JsonProperty
    protected String hubEntityId = "https://signin.service.gov.uk";

    // to generate a new cookie.key use the command `dd if=/dev/random count=1 bs=64 | base64`
    @NotNull
    @Valid
    @JsonProperty
    protected SecureCookieConfiguration secureCookieConfiguration;

    @NotNull
    @Valid
    @JsonProperty
    private EuropeanIdentityConfiguration europeanIdentity;

    @NotNull
    @Valid
    @JsonProperty("database")
    private DatabaseConfiguration databaseConfiguration;

    @Valid
    @JsonProperty
    private SingleIdpConfiguration singleIdpJourney;

    @NotNull
    @Valid
    @JsonProperty
    private StaleSessionReaperConfiguration staleSessionReaperConfiguration = new StaleSessionReaperConfiguration();

    @NotNull
    @Valid
    @JsonProperty
    private boolean isPrometheusEnabled = true;

    @NotNull
    @Valid
    @JsonProperty
    private boolean isHeadlessIdpEnabled = false;

    @NotNull
    @Valid
    @JsonProperty
    private boolean isIdpEnabled = true;

    protected StubIdpConfiguration() {
    }

    public String getHubEntityId() {
        return hubEntityId;
    }

    @Override
    public Duration getAssertionLifetime() {
        return assertionLifetime;
    }

    @Override
    public String getServiceName() {
        return serviceInfo.getName();
    }

    public SamlConfiguration getSamlConfiguration() {
        return saml;
    }

    public boolean isBasicAuthEnabledForUserResource() {
        return basicAuthEnabledForUserResource;
    }

    public String getStubIdpsYmlFileLocation() {
        return stubIdpsYmlFileLocation;
    }

    public Duration getStubIdpYmlFileRefresh() {
        return stubIdpYmlFileRefresh;
    }

    public String getAssetsCacheDuration() {
        return assetsCacheDuration;
    }

    public boolean shouldCacheAssets() {
        return shouldCacheAssets;
    }

    public SigningKeyPairConfiguration getSigningKeyPairConfiguration(){
        return signingKeyPairConfiguration;
    }

    public MetadataResolverConfiguration getMetadataConfiguration() {
        return metadata;
    }

    public SecureCookieConfiguration getSecureCookieConfiguration() {
        return secureCookieConfiguration;
    }

    public EuropeanIdentityConfiguration getEuropeanIdentityConfiguration() {
        return europeanIdentity;
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }

    public SingleIdpConfiguration getSingleIdpJourneyConfiguration() { return singleIdpJourney; }

    public StaleSessionReaperConfiguration getStaleSessionReaperConfiguration() {
        return staleSessionReaperConfiguration;
    }

    public boolean isPrometheusEnabled() {
        return isPrometheusEnabled;
    }

    public boolean isHeadlessIdpEnabled() {
        return isHeadlessIdpEnabled;
    }

    public boolean isIdpEnabled() {
        return isIdpEnabled;
    }
}
