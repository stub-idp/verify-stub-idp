package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.Address;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class AddressAttributeBuilder_1_1 {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private final List<Address> addresses = new ArrayList<>();

    private AddressAttributeBuilder_1_1() {}

    public static AddressAttributeBuilder_1_1 anAddressAttribute() {
        return new AddressAttributeBuilder_1_1();
    }

    public Attribute buildPreviousAddress() {
        return getAttribute(IdaConstants.Attributes_1_1.PreviousAddress.FRIENDLY_NAME, IdaConstants.Attributes_1_1.PreviousAddress.NAME);
    }

    public Attribute buildCurrentAddress() {
        return getAttribute(IdaConstants.Attributes_1_1.CurrentAddress.FRIENDLY_NAME, IdaConstants.Attributes_1_1.CurrentAddress.NAME);
    }

    public Attribute buildEidasCurrentAddress() {
        return getAttribute(IdaConstants.Eidas_Attributes.CurrentAddress.FRIENDLY_NAME, IdaConstants.Eidas_Attributes.CurrentAddress.NAME);
    }

    private Attribute getAttribute(String friendlyName, String name) {
        Attribute addressAttribute = openSamlXmlObjectFactory.createAttribute();

        for (Address address : addresses) {
            address.detach();
            addressAttribute.getAttributeValues().add(address);
        }

        addressAttribute.setFriendlyName(friendlyName);
        addressAttribute.setName(name);
        addressAttribute.setNameFormat(Attribute.UNSPECIFIED);

        return addressAttribute;
    }

    public AddressAttributeBuilder_1_1 addAddress(Address address) {
        this.addresses.add(address);
        return this;
    }
}
