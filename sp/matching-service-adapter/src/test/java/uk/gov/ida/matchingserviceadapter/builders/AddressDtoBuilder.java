package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddressDtoBuilder {
    private List<String> lines = new ArrayList<>();
    private Optional<String> postCode = Optional.empty();
    private Optional<String> internationalPostCode = Optional.empty();
    private Optional<String> uprn = Optional.empty();
    private LocalDate fromDate = LocalDate.parse("2001-01-01");
    private Optional<LocalDate> toDate = Optional.empty();
    private boolean verified = false;

    public VerifyAddressDto buildVerifyAddressDto() {
        return new VerifyAddressDto(
                lines,
                postCode,
                internationalPostCode,
                uprn,
                fromDate,
                toDate,
                verified);
    }

    public UniversalAddressDto buildUniversalAddressDto() {
        return new UniversalAddressDto(
                lines,
                postCode,
                internationalPostCode,
                uprn,
                fromDate,
                toDate,
                verified);
    }

    public AddressDtoBuilder withLines(final List<String> lines) {
        this.lines = lines;
        return this;
    }

    public AddressDtoBuilder withPostCode(final String postCode) {
        this.postCode = Optional.ofNullable(postCode);
        return this;
    }

    public AddressDtoBuilder withInternationalPostCode(final String internationalPostCode) {
        this.internationalPostCode = Optional.ofNullable(internationalPostCode);
        return this;
    }

    public AddressDtoBuilder withUPRN(final String uprn) {
        this.uprn = Optional.ofNullable(uprn);
        return this;
    }

    public AddressDtoBuilder withFromDate(final LocalDate fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public AddressDtoBuilder withToDate(final LocalDate toDate) {
        this.toDate = Optional.ofNullable(toDate);
        return this;
    }

    public AddressDtoBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}
