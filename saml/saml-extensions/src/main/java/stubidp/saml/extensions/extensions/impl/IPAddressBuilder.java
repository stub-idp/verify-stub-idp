package stubidp.saml.extensions.extensions.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.IPAddress;

public class IPAddressBuilder extends AbstractSAMLObjectBuilder<IPAddress> {

    @Override
    public IPAddress buildObject() {
        return buildObject(IPAddress.DEFAULT_ELEMENT_NAME);
    }

    @Override
    public IPAddress buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new IPAddressImpl(namespaceURI, localName, namespacePrefix);
    }
}
