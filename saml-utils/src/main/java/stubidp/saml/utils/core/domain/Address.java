package stubidp.saml.utils.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Address implements MdsAttributeValue, Serializable {
    private boolean verified;
    private Instant from;
    private Optional<Instant> to;
    private Optional<String> postCode;
    private List<String> lines;
    private Optional<String> internationalPostCode;
    private Optional<String> uprn;

    public Address(
            List<String> lines,
            String postCode,
            String internationalPostCode,
            String uprn,
            Instant from,
            Instant to,
            boolean verified) {

        this.internationalPostCode = Optional.ofNullable(internationalPostCode);
        this.uprn = Optional.ofNullable(uprn);
        this.from = from;
        this.postCode = Optional.ofNullable(postCode);
        this.lines = lines;
        this.to = Optional.ofNullable(to);
        this.verified = verified;
    }

    @JsonCreator
    public Address(
            @JsonProperty("lines") List<String> lines,
            @JsonProperty("postCode") Optional<String> postCode,
            @JsonProperty("internationalPostCode") Optional<String> internationalPostCode,
            @JsonProperty("uprn") Optional<String> uprn,
            @JsonProperty("from") Instant from,
            @JsonProperty("to") Optional<Instant> to,
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

    public Instant getFrom() {
        return from;
    }

    public Optional<Instant> getTo() {
        return to;
    }

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
}
