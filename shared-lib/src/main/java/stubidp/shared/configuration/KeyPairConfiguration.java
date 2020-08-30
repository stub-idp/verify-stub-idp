package stubidp.shared.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.utils.security.configuration.DeserializablePublicKeyConfiguration;
import stubidp.utils.security.configuration.PrivateKeyConfiguration;

import javax.validation.Valid;
import java.security.PrivateKey;
import java.util.Optional;

public class KeyPairConfiguration {

    @Valid
    @JsonProperty
    private String cert;

    @Valid
    @JsonProperty
    private DeserializablePublicKeyConfiguration publicKeyConfiguration;

    @Valid
    @JsonProperty
    private PrivateKeyConfiguration privateKeyConfiguration;

    public String getCert() {
        return Optional.ofNullable(cert).orElseGet(() -> stripHeaders(publicKeyConfiguration.getCert()));
    }

    public PrivateKey getPrivateKey() {
        return privateKeyConfiguration.getPrivateKey();
    }

    private String stripHeaders(String cert) {
        return cert.replace("-----BEGIN CERTIFICATE-----\n", "")
                .replace("-----END CERTIFICATE-----", "")
                .replace("\n", "")
                .trim();
    }
}
