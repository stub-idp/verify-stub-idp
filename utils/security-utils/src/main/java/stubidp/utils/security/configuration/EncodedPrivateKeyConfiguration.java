package stubidp.utils.security.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.PrivateKey;
import java.util.Base64;

@SuppressWarnings("unused")
public class EncodedPrivateKeyConfiguration extends PrivateKeyConfiguration {

    private final PrivateKey privateKey;

    @JsonCreator
    public EncodedPrivateKeyConfiguration(@JsonProperty("key") String key) {
        this.privateKey = getPrivateKeyFromBytes(Base64.getDecoder().decode(key));
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
