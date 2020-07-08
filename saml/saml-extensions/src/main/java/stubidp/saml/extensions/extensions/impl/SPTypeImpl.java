package stubidp.saml.extensions.extensions.impl;

import stubidp.saml.extensions.extensions.SPType;

public class SPTypeImpl extends StringValueSamlObjectImpl implements SPType {

    protected SPTypeImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }
}
