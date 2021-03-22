package stubidp.saml.extensions.extensions.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.InternationalPostCode;

public class InternationalPostCodeBuilder extends AbstractSAMLObjectBuilder<InternationalPostCode> {

    public InternationalPostCodeBuilder() {
    }

    @Override
    public InternationalPostCode buildObject() {
        return buildObject(InternationalPostCode.DEFAULT_ELEMENT_NAME);
    }

    @Override
    public InternationalPostCode buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new InternationalPostCodeImpl(namespaceURI, localName, namespacePrefix);
    }
}
