package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;

import java.time.LocalDate;

public class SimpleMdsValueDtoBuilder<T> {

    private T value = null;

    private LocalDate from = LocalDate.now().minusDays(5);
    private LocalDate to = LocalDate.now().plusDays(5);
    private boolean verified = false;

    public static <T> SimpleMdsValueDtoBuilder<T> aSimpleMdsValueDto() {
        return new SimpleMdsValueDtoBuilder<>();
    }

    public SimpleMdsValueDto<T> build() {
        return new SimpleMdsValueDto<>(value, from, to, verified);
    }

    public SimpleMdsValueDtoBuilder<T> withValue(T value) {
        this.value = value;
        return this;
    }

    public SimpleMdsValueDtoBuilder<T> withFrom(LocalDate from) {
        this.from = from;
        return this;
    }

    public SimpleMdsValueDtoBuilder<T> withTo(LocalDate to) {
        this.to = to;
        return this;
    }

    public SimpleMdsValueDtoBuilder<T> withVerifiedStatus(boolean verified) {
        this.verified = verified;
        return this;
    }
}
