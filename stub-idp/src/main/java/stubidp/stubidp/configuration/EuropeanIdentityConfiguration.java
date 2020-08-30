package stubidp.stubidp.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import stubidp.saml.metadata.MultiTrustStoresBackedMetadataConfiguration;
import stubidp.shared.configuration.KeyPairConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class EuropeanIdentityConfiguration {

    @NotNull
    @Valid
    @JsonProperty
    private String hubConnectorEntityId;

    @NotNull
    @Valid
    @JsonProperty
    private boolean enabled;

    @NotNull
    @Valid
    @JsonProperty
    private String stubCountryBaseUrl;

    @NotNull
    @Valid
    @JsonProperty
    private MultiTrustStoresBackedMetadataConfiguration metadata;

    @NotNull
    @Valid
    @JsonProperty
    protected KeyPairConfiguration signingKeyPairConfiguration;

    public String getHubConnectorEntityId() {
        return hubConnectorEntityId;
    }

    public MetadataResolverConfiguration getMetadata() {
        return metadata;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getStubCountryBaseUrl() {
        return stubCountryBaseUrl;
    }

    public KeyPairConfiguration getSigningKeyPairConfiguration() {
        return signingKeyPairConfiguration;
    }
}

