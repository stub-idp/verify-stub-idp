package uk.gov.ida.rp.testrp.contract;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniversalAddressDto {
    private boolean verified;

    @JsonAlias("fromDate")
    private DateTime from;
    @JsonAlias("toDate")
    private Optional<DateTime> to = Optional.empty();
    private Optional<String> postCode = Optional.empty();
    private List<String> lines;
    private Optional<String> internationalPostCode = Optional.empty();
    private Optional<String> uprn = Optional.empty();

    @SuppressWarnings("unused")
    private UniversalAddressDto() {
        // Needed by JAXB
    }

    public UniversalAddressDto(
            List<String> lines,
            Optional<String> postCode,
            Optional<String> internationalPostCode,
            Optional<String> uprn,
            DateTime from,
            Optional<DateTime> to,
            boolean verified) {

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

    public DateTime getFrom() {
        return from;
    }

    public Optional<DateTime> getTo() {
        return to;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniversalAddressDto that = (UniversalAddressDto) o;
        return verified == that.verified && Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(postCode, that.postCode) && Objects.equals(lines, that.lines) && Objects.equals(internationalPostCode, that.internationalPostCode) && Objects.equals(uprn, that.uprn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verified, from, to, postCode, lines, internationalPostCode, uprn);
    }

    @Override
    public String toString() {
        return "UniversalAddressDto{" +
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
