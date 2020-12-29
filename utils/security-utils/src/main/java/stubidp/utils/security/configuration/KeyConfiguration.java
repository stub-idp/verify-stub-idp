package stubidp.utils.security.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class KeyConfiguration {

    @Valid
    @Size(min = 1)
    @JsonProperty
    protected String keyUri;

    @Valid
    @Size(min = 1)
    @JsonProperty
    protected String base64EncodedKey;

    protected KeyConfiguration() {
    }

    public String getKeyUri() {
        return keyUri;
    }

    public String getBase64EncodedKey() {
        return base64EncodedKey;
    }
}
