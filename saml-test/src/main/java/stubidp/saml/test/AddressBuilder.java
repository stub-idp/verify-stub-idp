package stubidp.saml.test;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AddressBuilder {
    private List<String> lines = Collections.emptyList();
    private Optional<String> postCode = Optional.empty();
    private Optional<String> internationalPostCode = Optional.empty();
    private Optional<String> uprn = Optional.empty();
    private Instant fromDate = BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2001-01-01");
    private Instant toDate = null;
    private boolean verified = false;

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

    public AddressBuilder withFromDate(final Instant fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public AddressBuilder withToDate(final Instant toDate) {
        this.toDate = toDate;
        return this;
    }

    public AddressBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}
