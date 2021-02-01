package uk.gov.ida.rp.testrp.contract;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleMdsValueDto<T> {

    private T value;
    private DateTime from;
    private DateTime to;
    private boolean verified;

    @SuppressWarnings("unused")
    public SimpleMdsValueDto() {
        // needed for JAXB
    }

    public SimpleMdsValueDto(T value, DateTime from, DateTime to, boolean verified) {
        this.value = value;
        this.from = from;
        this.to = to;
        this.verified = verified;
    }

    public T getValue() {
        return value;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMdsValueDto<?> that = (SimpleMdsValueDto<?>) o;
        return verified == that.verified && Objects.equals(value, that.value) && Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, from, to, verified);
    }

    @Override
    public String toString() {
        return "SimpleMdsValueDto{" +
                "\nvalue=" + value +
                ",\n from=" + from +
                ",\n to=" + to +
                ",\n verified=" + verified +
                '}';
    }
}
