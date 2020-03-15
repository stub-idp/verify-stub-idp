package stubidp.saml.utils.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;

public class SimpleMdsValue<T> implements Serializable {

    private T value;
    private Instant from;
    private Instant to;
    private boolean verified;

    @JsonCreator
    public SimpleMdsValue(@JsonProperty("value") T value, @JsonProperty("from") Instant from, @JsonProperty("to") Instant to, @JsonProperty("verified") boolean verified) {
        this.value = value;
        this.from = from;
        this.to = to;
        this.verified = verified;
    }

    public T getValue() {
        return value;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public boolean isVerified() {
        return verified;
    }
}
