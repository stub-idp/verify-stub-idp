package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public abstract class AddressDto {
    private boolean verified;
    private Optional<String> postCode = Optional.empty();
    private List<String> lines;
    private Optional<String> internationalPostCode = Optional.empty();
    private Optional<String> uprn = Optional.empty();

    @SuppressWarnings("unused") // needed for JAXB
    protected AddressDto() {
    }

    public AddressDto(
            List<String> lines,
            Optional<String> postCode,
            Optional<String> internationalPostCode,
            Optional<String> uprn,
            boolean verified) {

        this.lines = lines;
        this.postCode = postCode;
        this.internationalPostCode = internationalPostCode;
        this.uprn = uprn;
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

    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressDto that = (AddressDto) o;
        return verified == that.verified && Objects.equals(postCode, that.postCode) && Objects.equals(lines, that.lines) && Objects.equals(internationalPostCode, that.internationalPostCode) && Objects.equals(uprn, that.uprn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verified, postCode, lines, internationalPostCode, uprn);
    }
}
