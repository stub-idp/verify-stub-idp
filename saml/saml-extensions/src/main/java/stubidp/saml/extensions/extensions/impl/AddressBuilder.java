package stubidp.saml.extensions.extensions.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.Address;

public class AddressBuilder extends AbstractSAMLObjectBuilder<Address> {

    public AddressBuilder() {
    }

    @Override
    public Address buildObject() {
        return buildObject(Address.DEFAULT_ELEMENT_NAME, Address.TYPE_NAME);
    }

    @Override
    public Address buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new AddressImpl(namespaceURI, localName, namespacePrefix, Address.TYPE_NAME);
    }
}
