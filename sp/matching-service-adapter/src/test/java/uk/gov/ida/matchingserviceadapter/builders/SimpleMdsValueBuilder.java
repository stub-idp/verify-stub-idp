package uk.gov.ida.matchingserviceadapter.builders;

import stubidp.saml.domain.assertions.SimpleMdsValue;

import java.time.LocalDate;

public class SimpleMdsValueBuilder<T> {

    public static final LocalDate DEFAULT_FROM_DATE = LocalDate.parse("2001-01-01");
    public static final LocalDate DEFAULT_HISTORICAL_TO_DATE = LocalDate.parse("2001-01-01");
    public static final LocalDate DEFAULT_HISTORICAL_FROM_DATE = LocalDate.parse("2000-01-01");

    private T value;

    private LocalDate from;
    private LocalDate to;
    private boolean verified = false;

    public static <T> SimpleMdsValueBuilder<T> aCurrentSimpleMdsValue() {
        return new SimpleMdsValueBuilder<T>()
                .withFrom(DEFAULT_FROM_DATE)
                .withTo(null);
    }

    public static <T> SimpleMdsValueBuilder<T> aHistoricalSimpleMdsValue() {
        return new SimpleMdsValueBuilder<T>()
                .withTo(DEFAULT_HISTORICAL_TO_DATE)
                .withFrom(DEFAULT_HISTORICAL_FROM_DATE);
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
