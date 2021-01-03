package stubidp.saml.utils.core.domain;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.extensions.extensions.Line;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AddressFactory {

    private AddressFactory() {}

    public static Address create(List<String> lines, String postCode, String internationalPostCode, String uprn, String from, String to, boolean isVerified) {
        Instant fromInstant = BaseMdsSamlObjectUnmarshaller.InstantFromDate.of(from);
        Instant toInstant = BaseMdsSamlObjectUnmarshaller.InstantFromDate.of(to);
        return new Address(lines, postCode, internationalPostCode, uprn, fromInstant, toInstant, isVerified);
    }

    public static Address create(List<String> lines, String postCode, String internationalPostCode, String uprn, Instant from, Instant to, boolean isVerified) {
        return new Address(lines, postCode, internationalPostCode, uprn, from, to, isVerified);
    }

    public static Address createNoDates(List<String> lines, String postCode, String internationalPostCode, String uprn, boolean isVerified) {
        return new Address(lines, postCode, internationalPostCode, uprn, null, null, isVerified);
    }

    public static List<Address> create(Attribute attribute) {
        List<Address> addresses = new ArrayList<>();
        for (XMLObject xmlObject : attribute.getAttributeValues()) {
            stubidp.saml.extensions.extensions.Address address = (stubidp.saml.extensions.extensions.Address) xmlObject;
            addresses.add(create(address));
        }
        return addresses;
    }

    public static Address create(stubidp.saml.extensions.extensions.Address addressAttributeValue) {
        List<String> lines = new ArrayList<>();
        for (Line originalLine : addressAttributeValue.getLines()) {
            lines.add(originalLine.getValue());
        }

        Instant toDate = addressAttributeValue.getTo();

        Instant fromDate = addressAttributeValue.getFrom();

        String postCodeString = null;
        if (addressAttributeValue.getPostCode() != null) {
            postCodeString = addressAttributeValue.getPostCode().getValue();
        }

        String internationalPostCodeString = null;
        if (addressAttributeValue.getInternationalPostCode() != null) {
            internationalPostCodeString = addressAttributeValue.getInternationalPostCode().getValue();
        }

        String uprnString = null;
        if (addressAttributeValue.getUPRN() != null) {
            uprnString = addressAttributeValue.getUPRN().getValue();
        }

        return new Address(lines, postCodeString, internationalPostCodeString, uprnString, fromDate, toDate, addressAttributeValue.getVerified());
    }
}
