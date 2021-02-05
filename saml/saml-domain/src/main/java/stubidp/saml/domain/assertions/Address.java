package stubidp.saml.domain.assertions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Address implements MdsAttributeValue, Serializable {
    private final boolean verified;
    private final LocalDate from;
    private final LocalDate to;
    private final Optional<String> postCode;
    private final List<String> lines;
    private final Optional<String> internationalPostCode;
    private final Optional<String> uprn;

    public Address(
            List<String> lines,
            String postCode,
            String internationalPostCode,
            String uprn,
            LocalDate from,
            LocalDate to,
            boolean verified) {

        this.internationalPostCode = Optional.ofNullable(internationalPostCode);
        this.uprn = Optional.ofNullable(uprn);
        this.from = from;
        this.postCode = Optional.ofNullable(postCode);
        this.lines = lines;
        this.to = to;
        this.verified = verified;
    }

    @JsonCreator
    public Address(
            @JsonProperty("lines") List<String> lines,
            @JsonProperty("postCode") Optional<String> postCode,
            @JsonProperty("internationalPostCode") Optional<String> internationalPostCode,
            @JsonProperty("uprn") Optional<String> uprn,
            @JsonProperty("from") LocalDate from,
            @JsonProperty("to") LocalDate to,
            @JsonProperty("verified") boolean verified) {
        this.lines = lines;
        this.postCode = postCode;
        this.internationalPostCode = internationalPostCode;
        this.uprn = uprn;
        this.from = from;
        this.to = to;
        this.verified = verified;
    }

    public List<String> getLines() {
        return lines;
    }

    public Optional<String> getPostCode() {
        return postCode;
    }

    public Optional<String> getInternationalPostCode() {
        return internationalPostCode;
    }

    public Optional<String> getUPRN() {
        return uprn;
    }

    @Override
    public LocalDate getFrom() {
        return from;
    }

    @Override
    public LocalDate getTo() {
        return to;
    }

    @Override
    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return verified == address.verified &&
                Objects.equals(from, address.from) &&
                Objects.equals(to, address.to) &&
                Objects.equals(postCode, address.postCode) &&
                Objects.equals(lines, address.lines) &&
                Objects.equals(internationalPostCode, address.internationalPostCode) &&
                Objects.equals(uprn, address.uprn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verified, from, to, postCode, lines, internationalPostCode, uprn);
    }

    @Override
    public String toString() {
        return "Address{" +
                "verified=" + verified +
                ", from=" + from +
                ", to=" + to +
                ", postCode=" + postCode +
                ", lines=" + lines +
                ", internationalPostCode=" + internationalPostCode +
                ", uprn=" + uprn +
                '}';
    }
}
