package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.Address;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AddressBuilder {
    private List<String> lines = Collections.emptyList();
    private Optional<String> postCode = Optional.empty();
    private Optional<String> internationalPostCode = Optional.empty();
    private Optional<String> uprn = Optional.empty();
    private LocalDate fromDate = LocalDate.parse("2001-01-01");
    private LocalDate toDate = null;
    private boolean verified = false;

    private AddressBuilder() {}

    public static AddressBuilder anAddress() {
        return new AddressBuilder();
    }

    public Address build() {
        return new Address(
                lines,
                postCode,
                internationalPostCode,
                uprn,
                fromDate,
                toDate,
                verified);
    }

    public AddressBuilder withLines(final List<String> lines) {
        this.lines = lines;
        return this;
    }

    public AddressBuilder withPostCode(final String postCode) {
        this.postCode = Optional.ofNullable(postCode);
        return this;
    }

    public AddressBuilder withInternationalPostCode(final String internationalPostCode) {
        this.internationalPostCode = Optional.ofNullable(internationalPostCode);
        return this;
    }

    public AddressBuilder withUPRN(final String uprn) {
        this.uprn = Optional.ofNullable(uprn);
        return this;
    }

    public AddressBuilder withFromDate(final LocalDate fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public AddressBuilder withToDate(final LocalDate toDate) {
        this.toDate = toDate;
        return this;
    }

    public AddressBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}
