package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.utils.security.configuration.DeserializablePublicKeyConfiguration;
import stubidp.utils.security.configuration.PrivateKeyConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class KeyPairConfiguration {
    @NotNull
    @Valid
    @JsonProperty
    private DeserializablePublicKeyConfiguration publicKey;

    @NotNull
    @Valid
    @JsonProperty
    private PrivateKeyConfiguration privateKey;

    protected KeyPairConfiguration() {}

    public DeserializablePublicKeyConfiguration getPublicKey() {
        return publicKey;
    }

    public PrivateKeyConfiguration getPrivateKey() {
        return privateKey;
    }
}
