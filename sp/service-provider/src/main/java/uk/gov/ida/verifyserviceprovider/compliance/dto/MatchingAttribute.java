package uk.gov.ida.verifyserviceprovider.compliance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

public class MatchingAttribute {
    @NotNull
    @JsonProperty
    private String value;
    @JsonProperty @JsonInclude(Include.NON_NULL)
    private LocalDateTime from;
    @JsonProperty @JsonInclude(Include.NON_NULL)
    private LocalDateTime to;
    @NotNull
    @JsonProperty
    private boolean verified;

    public MatchingAttribute() {};

    public String getValue() {
        return value;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public boolean isVerified() {
        return verified;
    }

    public MatchingAttribute(String value) {
        this.value = value;
        this.verified = true;
    }

    public MatchingAttribute(
            final String value,
            final boolean verified,
            final LocalDateTime from,
            final LocalDateTime to) {

        this.value = value;
        this.verified = verified;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchingAttribute that = (MatchingAttribute) o;
        return verified == that.verified && Objects.equals(value, that.value) && Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, from, to, verified);
    }
}