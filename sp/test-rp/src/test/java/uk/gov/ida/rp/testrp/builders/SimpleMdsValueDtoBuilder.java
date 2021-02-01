package uk.gov.ida.rp.testrp.builders;

import org.joda.time.DateTime;
import uk.gov.ida.rp.testrp.contract.SimpleMdsValueDto;

public class SimpleMdsValueDtoBuilder<T> {

    private static final int DEFAULT_DAYS_DELTA = 5;

    private T value = null;

    private DateTime from = DateTime.now().minusDays(DEFAULT_DAYS_DELTA);
    private DateTime to = DateTime.now().plusDays(DEFAULT_DAYS_DELTA);
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

    public SimpleMdsValueDtoBuilder<T> withFrom(DateTime from) {
        this.from = from;
        return this;
    }

    public SimpleMdsValueDtoBuilder<T> withTo(DateTime to) {
        this.to = to;
        return this;
    }

    public SimpleMdsValueDtoBuilder<T> withVerifiedStatus(boolean verified) {
        this.verified = verified;
        return this;
    }
}
