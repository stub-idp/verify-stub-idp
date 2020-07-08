package stubidp.saml.metadata;

import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;

public abstract class MetadataConfiguration implements MetadataResolverConfiguration {

    @NotNull
    @Valid
    /* HTTP{S} URL the SAML metadata can be loaded from */
    private URI uri;

    @NotNull
    @Valid
    /* Used to set {@link org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider#minRefreshDelay} */
    private Duration minRefreshDelay;

    @NotNull
    @Valid
    /* Used to set {@link org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider#maxRefreshDelay} */
    private Duration maxRefreshDelay;

    @NotNull
    @Valid
    /*
     * What entityId can be expected to reliably appear in the SAML metadata?
     * Used to provide a healthcheck {@link uk.gov.ida.saml.dropwizard.metadata.MetadataHealthCheck}
     */
    private String expectedEntityId;

    @NotNull
    @Valid
    private JerseyClientConfiguration client;

    @NotNull
    @Valid
    private String jerseyClientName;

    @NotNull
    @Valid
    private String hubFederationId;

    public MetadataConfiguration(URI uri,
                                 Duration minRefreshDelay,
                                 Duration maxRefreshDelay,
                                 String expectedEntityId,
                                 JerseyClientConfiguration client,
                                 String jerseyClientName,
                                 String hubFederationId
    ) {
        this.uri = uri;
        this.minRefreshDelay = Optional.ofNullable(minRefreshDelay).orElse(Duration.ofMillis(60000L));
        this.maxRefreshDelay = Optional.ofNullable(maxRefreshDelay).orElse(Duration.ofMillis(600000L));
        this.expectedEntityId = Optional.ofNullable(expectedEntityId).orElseThrow();
        this.client = Optional.ofNullable(client).orElse(new JerseyClientConfiguration());
        this.jerseyClientName = Optional.ofNullable(jerseyClientName).orElse("MetadataClient");
        this.hubFederationId = Optional.ofNullable(hubFederationId).orElse("VERIFY-FEDERATION");
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public Duration getMinRefreshDelay() {
        return minRefreshDelay;
    }

    @Override
    public Duration getMaxRefreshDelay() {
        return maxRefreshDelay;
    }

    @Override
    public String getExpectedEntityId() {
        return expectedEntityId;
    }

    @Override
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return client;
    }

    @Override
    public String getJerseyClientName() {
        return jerseyClientName;
    }

    @Override
    public String getHubFederationId() {
        return hubFederationId;
    }
}
