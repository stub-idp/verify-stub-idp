package stubidp.saml.extensions.extensions.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;
import stubidp.saml.extensions.extensions.LocalisableAttributeValue;

public class LocalisableStringBasedMdsAttributeValueUnmarshaller extends StringBasedMdsAttributeValueUnmarshaller {

    public LocalisableStringBasedMdsAttributeValueUnmarshaller() {
    }

    @Override
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        LocalisableAttributeValue localisableAttributeValue = (LocalisableAttributeValue) samlObject;
        if (attribute.getLocalName().equals(LocalisableAttributeValue.LANGUAGE_ATTRIB_NAME)) {
            localisableAttributeValue.setLanguage(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
}
