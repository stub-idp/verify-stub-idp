package stubidp.saml.domain.assertions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

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


    @Override
    public String toString() {
        return "SimpleMdsValue{" +
                "value=" + value +
                ", from=" + from +
                ", to=" + to +
                ", verified=" + verified +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMdsValue<?> that = (SimpleMdsValue<?>) o;
        return verified == that.verified &&
                Objects.equals(value, that.value) &&
                Objects.equals(from, that.from) &&
                Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, from, to, verified);
    }
}
