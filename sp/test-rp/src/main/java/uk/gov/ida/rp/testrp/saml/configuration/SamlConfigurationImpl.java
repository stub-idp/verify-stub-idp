package uk.gov.ida.rp.testrp.saml.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.saml.domain.configuration.SamlConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class SamlConfigurationImpl implements SamlConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    protected String entityId;

    @Valid
    @NotNull
    @JsonProperty
    protected URI expectedDestination;

    protected SamlConfigurationImpl() {}

    public SamlConfigurationImpl(String entityId, URI expectedDestination) {
        this.entityId = entityId;
        this.expectedDestination = expectedDestination;
    }

    @Override
    public URI getExpectedDestinationHost() {
        return expectedDestination;
    }

    public String getEntityId() {
        return entityId;
    }
}
