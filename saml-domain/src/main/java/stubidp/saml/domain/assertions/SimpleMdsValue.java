package stubidp.saml.domain.assertions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;

public class SimpleMdsValue<T> implements MdsAttributeValue, Serializable {
    private final T value;
    private final Instant from;
    private final Instant to;
    private final boolean verified;

    @JsonCreator
    public SimpleMdsValue(@JsonProperty("value") T value,
                          @JsonProperty("from") Instant from,
                          @JsonProperty("to") Instant to,
                          @JsonProperty("verified") boolean verified) {
        this.value = value;
        this.from = from;
        this.to = to;
        this.verified = verified;
    }

    public T getValue() {
        return value;
    }

    @Override
    public Instant getFrom() {
        return from;
    }

    @Override
    public Instant getTo() {
        return to;
    }

    @Override
    public boolean isVerified() {
        return verified;
    }
}
