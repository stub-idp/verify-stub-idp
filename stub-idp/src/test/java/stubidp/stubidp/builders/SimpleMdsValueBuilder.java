package stubidp.stubidp.builders;

import stubidp.stubidp.domain.MatchingDatasetValue;

import java.time.Instant;
import java.time.ZoneId;

public class SimpleMdsValueBuilder<T> {

    private T value = null;
    private Instant from = Instant.now().atZone(ZoneId.of("UTC")).minusDays(5).toInstant();
    private Instant to = Instant.now();
    private boolean verified = false;

    public static <T> SimpleMdsValueBuilder<T> aSimpleMdsValue() {
        return new SimpleMdsValueBuilder<>();
    }

    public MatchingDatasetValue<T> build() {
        return new MatchingDatasetValue<>(value, from, to, verified);
    }

    public SimpleMdsValueBuilder<T> withValue(T value) {
        this.value = value;
        return this;
    }

    public SimpleMdsValueBuilder<T> withFrom(Instant from) {
        this.from = from;
        return this;
    }

    public SimpleMdsValueBuilder<T> withTo(Instant to) {
        this.to = to;
        return this;
    }

    public SimpleMdsValueBuilder<T> withVerifiedStatus(boolean verified) {
        this.verified = verified;
        return this;
    }
}
