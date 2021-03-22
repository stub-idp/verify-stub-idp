package stubidp.saml.extensions.extensions.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;

public class StringBasedMdsAttributeValueBuilder extends AbstractSAMLObjectBuilder<StringBasedMdsAttributeValue> {

    public StringBasedMdsAttributeValueBuilder() {
    }

    @Override
    public StringBasedMdsAttributeValue buildObject() {
        return buildObject(StringBasedMdsAttributeValue.DEFAULT_ELEMENT_NAME);
    }

    @Override
    public StringBasedMdsAttributeValue buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new StringBasedMdsAttributeValueImpl(namespaceURI, localName, namespacePrefix);
    }
}
