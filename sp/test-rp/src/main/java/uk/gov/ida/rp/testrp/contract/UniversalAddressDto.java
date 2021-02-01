package uk.gov.ida.rp.testrp.contract;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize()
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
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
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
