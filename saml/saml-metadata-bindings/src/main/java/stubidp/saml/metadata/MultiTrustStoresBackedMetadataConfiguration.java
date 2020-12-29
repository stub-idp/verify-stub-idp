package stubidp.saml.metadata;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import java.net.URI;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MultiTrustStoresBackedMetadataConfiguration extends TrustStoreBackedMetadataConfiguration {

    @Valid
    private final TrustStoreConfiguration spTrustStore;

    @Valid
    private final TrustStoreConfiguration idpTrustStore;

    @JsonCreator
    public MultiTrustStoresBackedMetadataConfiguration(
        @JsonProperty("uri") @JsonAlias({ "url" }) URI uri,
        @JsonProperty("minRefreshDelay") Duration minRefreshDelay,
        @JsonProperty("maxRefreshDelay") Duration maxRefreshDelay,
        @JsonProperty("expectedEntityId") String expectedEntityId,
        @JsonProperty("client") JerseyClientConfiguration client,
        @JsonProperty("jerseyClientName") String jerseyClientName,
        @JsonProperty("hubFederationId") String hubFederationId,
        @JsonProperty("trustStore") TrustStoreConfiguration trustStore,
        @JsonProperty("spTrustStore") TrustStoreConfiguration spTrustStore,
        @JsonProperty("idpTrustStore") TrustStoreConfiguration idpTrustStore) {

        super(uri, minRefreshDelay, maxRefreshDelay, expectedEntityId, client, jerseyClientName, hubFederationId, trustStore);
        this.spTrustStore = spTrustStore;
        this.idpTrustStore = idpTrustStore;
    }

    @Override
    public Optional<KeyStore> getSpTrustStore() {
        return Objects.isNull(spTrustStore)?Optional.empty():Optional.of(spTrustStore.getTrustStore());
    }

    @Override
    public Optional<KeyStore> getIdpTrustStore() {
        return Objects.isNull(idpTrustStore)?Optional.empty():Optional.of(idpTrustStore.getTrustStore());
    }
}
