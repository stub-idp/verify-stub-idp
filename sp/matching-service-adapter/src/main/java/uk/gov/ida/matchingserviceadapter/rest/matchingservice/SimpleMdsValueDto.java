package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleMdsValueDto<T> {

    private T value;
    private LocalDate from;
    private LocalDate to;
    private boolean verified;

    @SuppressWarnings("unused") // needed for JAXB
    public SimpleMdsValueDto() {}

    public SimpleMdsValueDto(T value, LocalDate from, LocalDate to, boolean verified) {
        this.value = value;
        this.from = from;
        this.to = to;
        this.verified = verified;
    }

    public T getValue() {
        return value;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
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
}
