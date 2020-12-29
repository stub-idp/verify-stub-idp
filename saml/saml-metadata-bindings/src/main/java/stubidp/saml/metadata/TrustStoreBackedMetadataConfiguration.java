package stubidp.saml.metadata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.KeyStore;
import java.time.Duration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrustStoreBackedMetadataConfiguration extends MetadataConfiguration {

    @NotNull
    @Valid
    private final TrustStoreConfiguration trustStore;

    @JsonCreator
    public TrustStoreBackedMetadataConfiguration(
        @JsonProperty("uri") @JsonAlias({ "url" }) URI uri,
        @JsonProperty("minRefreshDelay") Duration minRefreshDelay,
        @JsonProperty("maxRefreshDelay") Duration maxRefreshDelay,
        @JsonProperty("expectedEntityId") String expectedEntityId,
        @JsonProperty("client") JerseyClientConfiguration client,
        @JsonProperty("jerseyClientName") String jerseyClientName,
        @JsonProperty("hubFederationId") String hubFederationId,
        @JsonProperty("trustStore") TrustStoreConfiguration trustStore
    ) {
        super(uri, minRefreshDelay, maxRefreshDelay, expectedEntityId, client, jerseyClientName, hubFederationId);
        this.trustStore = trustStore;
    }

    @Override
    public KeyStore getTrustStore() {
        return trustStore.getTrustStore();
    }
}
