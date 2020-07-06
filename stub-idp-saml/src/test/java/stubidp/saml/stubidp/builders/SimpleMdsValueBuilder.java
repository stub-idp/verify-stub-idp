package stubidp.saml.stubidp.builders;

import stubidp.saml.domain.assertions.SimpleMdsValue;

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

    public SimpleMdsValue<T> build() {
        return new SimpleMdsValue<>(value, from, to, verified);
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
