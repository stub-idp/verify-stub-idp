package stubidp.saml.extensions.extensions.impl;

import stubidp.saml.extensions.extensions.StatusValue;

public class StatusValueImpl extends StringValueSamlObjectImpl implements StatusValue {

    protected StatusValueImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }
}
