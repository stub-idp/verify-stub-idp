package stubidp.saml.extensions.extensions.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.RequestedAttribute;

public class RequestedAttributeBuilder extends AbstractSAMLObjectBuilder<RequestedAttribute> {

    public RequestedAttributeBuilder() {
    }

    @NonNull
    @Override
    public RequestedAttribute buildObject() {
        return buildObject(IdaConstants.EIDAS_NS, RequestedAttribute.DEFAULT_ELEMENT_LOCAL_NAME, IdaConstants.EIDAS_PREFIX);
    }

    @NonNull
    @Override
    public RequestedAttribute buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new RequestedAttributeImpl(namespaceURI, localName, namespacePrefix);
    }
}

