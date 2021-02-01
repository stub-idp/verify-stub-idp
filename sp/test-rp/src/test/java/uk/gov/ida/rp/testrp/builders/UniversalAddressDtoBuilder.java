package uk.gov.ida.rp.testrp.builders;

import org.joda.time.DateTime;
import uk.gov.ida.rp.testrp.contract.UniversalAddressDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;

public class UniversalAddressDtoBuilder {

    private List<String> lines = new ArrayList<>();
    private Optional<String> postCode = empty();
    private Optional<String> internationalPostCode = empty();
    private Optional<String> uprn = empty();
    private DateTime from = DateTime.parse("2001-01-01");
    private Optional<DateTime> to = empty();
    private boolean verified = false;

    public static UniversalAddressDtoBuilder aUniversalAddressDto() {
        return new UniversalAddressDtoBuilder();
    }

    public UniversalAddressDto build() {
        return new UniversalAddressDto(
                lines,
                postCode,
                internationalPostCode,
                uprn,
                from,
                to,
                verified);
    }

    public UniversalAddressDtoBuilder withLines(final List<String> lines) {
        this.lines = lines;
        return this;
    }

    public UniversalAddressDtoBuilder withPostCode(final String postCode) {
        this.postCode = Optional.ofNullable(postCode);
        return this;
    }

    public UniversalAddressDtoBuilder withInternationalPostCode(final String internationalPostCode) {
        this.internationalPostCode = Optional.ofNullable(internationalPostCode);
        return this;
    }

    public UniversalAddressDtoBuilder withUPRN(final String uprn) {
        this.uprn = Optional.ofNullable(uprn);
        return this;
    }

    public UniversalAddressDtoBuilder withFrom(final DateTime from) {
        this.from = from;
        return this;
    }

    public UniversalAddressDtoBuilder withTo(final DateTime to) {
        this.to = Optional.ofNullable(to);
        return this;
    }

    public UniversalAddressDtoBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}
