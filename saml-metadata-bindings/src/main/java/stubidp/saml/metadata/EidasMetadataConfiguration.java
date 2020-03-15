package stubidp.saml.metadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;

import java.net.URI;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Optional;

public class EidasMetadataConfiguration {

    private URI trustAnchorUri;

    /* Used to set {@link org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider#minRefreshDelay} */
    private Duration minRefreshDelay;

    /* Used to set {@link org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider#maxRefreshDelay} */
    private Duration maxRefreshDelay;

    private Duration trustAnchorMaxRefreshDelay;

    private Duration trustAnchorMinRefreshDelay;

    private JerseyClientConfiguration client;

    private String jerseyClientName;

    private TrustStoreConfiguration trustStore;

    private URI metadataSourceUri;

    @JsonCreator
    public EidasMetadataConfiguration(@JsonProperty("trustAnchorUri") URI trustAnchorUri,
                                      @JsonProperty("minRefreshDelay") Duration minRefreshDelay,
                                      @JsonProperty("maxRefreshDelay") Duration maxRefreshDelay,
                                      @JsonProperty("trustAnchorMaxRefreshDelay") Duration trustAnchorMaxRefreshDelay,
                                      @JsonProperty("trustAnchorMinRefreshDelay") Duration trustAnchorMinRefreshDelay,
                                      @JsonProperty("client") JerseyClientConfiguration client,
                                      @JsonProperty("jerseyClientName") String jerseyClientName,
                                      @JsonProperty("trustStore") TrustStoreConfiguration trustStore,
                                      @JsonProperty("metadataSourceUri") URI metadataSourceUri
    )
    {
        this.trustAnchorUri = trustAnchorUri;
        this.minRefreshDelay = Optional.ofNullable(minRefreshDelay).orElse(Duration.ofMillis(60000L));
        this.maxRefreshDelay = Optional.ofNullable(maxRefreshDelay).orElse(Duration.ofMillis(600000L));
        this.trustAnchorMinRefreshDelay = Optional.ofNullable(trustAnchorMinRefreshDelay).orElse(Duration.ofMillis(60000L));
        this.trustAnchorMaxRefreshDelay = Optional.ofNullable(trustAnchorMaxRefreshDelay).orElse(Duration.ofMillis(3600000L));
        this.client = Optional.ofNullable(client).orElse(new JerseyClientConfiguration());
        this.jerseyClientName = Optional.ofNullable(jerseyClientName).orElse("MetadataClient");
        this.trustStore = trustStore;
        this.metadataSourceUri = metadataSourceUri;
    }

    public URI getTrustAnchorUri() {
        return trustAnchorUri;
    }

    public Duration getMinRefreshDelay() {
        return minRefreshDelay;
    }

    public Duration getMaxRefreshDelay() {
        return maxRefreshDelay;
    }

    public Duration getTrustAnchorMaxRefreshDelay() {
        return trustAnchorMaxRefreshDelay;
    }

    public Duration getTrustAnchorMinRefreshDelay() {
        return trustAnchorMinRefreshDelay;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return client;
    }

    public String getJerseyClientName() {
        return jerseyClientName;
    }

    public KeyStore getTrustStore() {
        return trustStore.getTrustStore();
    }

    public URI getMetadataSourceUri() {
        return metadataSourceUri;
    }
}
