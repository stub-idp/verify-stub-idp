package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.SimpleMdsValue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class SimpleMdsValueBuilder<T> {
    private T value = null;
    private LocalDate from = LocalDate.now().minusDays(5);
    private LocalDate to = LocalDate.now();
    private boolean verified = false;

    private SimpleMdsValueBuilder() {}

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

    public SimpleMdsValueBuilder<T> withFrom(LocalDate from) {
        this.from = from;
        return this;
    }

    public SimpleMdsValueBuilder<T> withTo(LocalDate to) {
        this.to = to;
        return this;
    }

    public SimpleMdsValueBuilder<T> withVerifiedStatus(boolean verified) {
        this.verified = verified;
        return this;
    }
}
