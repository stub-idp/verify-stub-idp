package stubidp.stubidp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.saml.utils.core.domain.SimpleMdsValue;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class MatchingDatasetValue<T> implements Serializable {

    private T value;
    private Instant from;
    private Instant to;
    private boolean verified;

    @JsonCreator
    public MatchingDatasetValue(@JsonProperty("value") T value,
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

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public String toString() {
        return "SimpleMdsValue2{" +
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
        MatchingDatasetValue<?> that = (MatchingDatasetValue<?>) o;
        return verified == that.verified &&
                Objects.equals(value, that.value) &&
                Objects.equals(from, that.from) &&
                Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, from, to, verified);
    }

    public SimpleMdsValue<T> asSimpleMdsValue() {
        return new SimpleMdsValue<>(value, from, to, verified);
    }

    public static <Z> MatchingDatasetValue<Z> fromSimpleMdsValue(SimpleMdsValue<Z> simpleMdsValue) {
        return new MatchingDatasetValue<>(
                simpleMdsValue.getValue(),
                simpleMdsValue.getFrom(),
                simpleMdsValue.getTo(),
                simpleMdsValue.isVerified()
        );
    }
}
