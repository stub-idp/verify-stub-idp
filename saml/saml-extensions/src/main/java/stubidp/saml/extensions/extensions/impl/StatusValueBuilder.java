package stubidp.saml.extensions.extensions.impl;


import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.StatusValue;

public class StatusValueBuilder extends AbstractSAMLObjectBuilder<StatusValue> {

    @Override
    public StatusValue buildObject() {
        return buildObject(StatusValue.DEFAULT_ELEMENT_NAME);
    }

    @Override
    public StatusValue buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new StatusValueImpl(namespaceURI, localName, namespacePrefix);
    }
}
