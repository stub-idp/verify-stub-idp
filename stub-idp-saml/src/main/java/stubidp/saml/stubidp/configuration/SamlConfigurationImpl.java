package stubidp.saml.stubidp.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class SamlConfigurationImpl implements SamlConfiguration {
    protected SamlConfigurationImpl() {
    }

    public SamlConfigurationImpl(String entityId, URI expectedDestination) {
        this.entityId = entityId;
        this.expectedDestination = expectedDestination;
    }

    @Valid
    @NotNull
    @JsonProperty
    protected String entityId;

    @Valid
    @NotNull
    @JsonProperty
    protected URI expectedDestination;

    @Override
    public URI getExpectedDestinationHost() {
        return expectedDestination;
    }

    public String getEntityId() {
        return entityId;
    }
}
