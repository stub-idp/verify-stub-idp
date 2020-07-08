package stubidp.saml.extensions.extensions.impl;

import stubidp.saml.extensions.extensions.UPRN;

public class UPRNImpl extends StringValueSamlObjectImpl implements UPRN {

    protected UPRNImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }
}
