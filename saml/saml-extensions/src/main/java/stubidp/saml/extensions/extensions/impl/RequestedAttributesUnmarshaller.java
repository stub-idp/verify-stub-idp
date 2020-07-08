package stubidp.saml.extensions.extensions.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.impl.AttributeUnmarshaller;
import stubidp.saml.extensions.extensions.RequestedAttribute;
import stubidp.saml.extensions.extensions.RequestedAttributes;

class RequestedAttributesUnmarshaller extends AttributeUnmarshaller {

    @Override
    protected void processChildElement(XMLObject parentObject, XMLObject childObject) throws UnmarshallingException {
        RequestedAttributes requestedAttributes = (RequestedAttributes) parentObject;

        if (childObject instanceof RequestedAttribute) {
            requestedAttributes.addRequestedAttribute((RequestedAttribute)childObject);
        } else {
            super.processChildElement(parentObject, childObject);
        }
    }
}

