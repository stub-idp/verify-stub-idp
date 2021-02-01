package uk.gov.ida.rp.testrp.saml.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class SamlConfiguration {
    @Valid
    @NotNull
    @JsonProperty
    protected String entityId;

    public String getEntityId() {
        return entityId;
    }
}
