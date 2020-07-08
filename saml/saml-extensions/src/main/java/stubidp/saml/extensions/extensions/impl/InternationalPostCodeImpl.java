package stubidp.saml.extensions.extensions.impl;

import stubidp.saml.extensions.extensions.InternationalPostCode;

public class InternationalPostCodeImpl extends StringValueSamlObjectImpl implements InternationalPostCode {

    protected InternationalPostCodeImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }
}
