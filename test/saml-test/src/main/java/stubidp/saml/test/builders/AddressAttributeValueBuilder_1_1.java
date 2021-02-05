package stubidp.saml.test.builders;

import stubidp.saml.extensions.extensions.Address;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddressAttributeValueBuilder_1_1 {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private Optional<LocalDate> from = Optional.empty();
    private Optional<LocalDate> to = Optional.empty();

    private final List<String> lines = new ArrayList<>();
    private Optional<String> postCode = Optional.of("RG99 1YY");
    private Optional<String> internationalPostCode = Optional.of("RG88 1ZZ");
    private Optional<String> uprn = Optional.of("79347894352");

    private boolean verified = false;

    private AddressAttributeValueBuilder_1_1() {}

    public static AddressAttributeValueBuilder_1_1 anAddressAttributeValue() {
        return new AddressAttributeValueBuilder_1_1();
    }

    public Address build() {
        Address addressAttributeValue = openSamlXmlObjectFactory.createAddressAttributeValue();
        from.ifPresent(addressAttributeValue::setFrom);
        to.ifPresent(addressAttributeValue::setTo);
        addressAttributeValue.setVerified(verified);
        for (String line : lines) {
            addressAttributeValue.getLines().add(openSamlXmlObjectFactory.createLine(line));
        }
        postCode.ifPresent(s -> addressAttributeValue.setPostCode(openSamlXmlObjectFactory.createPostCode(s)));
        internationalPostCode.ifPresent(s -> addressAttributeValue.setInternationalPostCode(openSamlXmlObjectFactory.createInternationalPostCode(s)));
        uprn.ifPresent(s -> addressAttributeValue.setUPRN(openSamlXmlObjectFactory.createUPRN(s)));
        return addressAttributeValue;
    }

    public AddressAttributeValueBuilder_1_1 withFrom(LocalDate from) {
        this.from = Optional.ofNullable(from);
        return this;
    }

    public AddressAttributeValueBuilder_1_1 withTo(LocalDate to) {
        this.to = Optional.ofNullable(to);
        return this;
    }

    public AddressAttributeValueBuilder_1_1 addLines(List<String> lines) {
        this.lines.addAll(lines);
        return this;
    }

    public AddressAttributeValueBuilder_1_1 withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public AddressAttributeValueBuilder_1_1 withPostcode(String postCode) {
        this.postCode = Optional.ofNullable(postCode);
        return this;
    }

    public AddressAttributeValueBuilder_1_1 withInternationalPostcode(String internationalPostcode) {
        this.internationalPostCode = Optional.ofNullable(internationalPostcode);
        return this;
    }

    public AddressAttributeValueBuilder_1_1 withUprn(String uprn) {
        this.uprn = Optional.ofNullable(uprn);
        return this;
    }
}
