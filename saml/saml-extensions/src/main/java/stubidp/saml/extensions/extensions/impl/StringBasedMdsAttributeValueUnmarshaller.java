package stubidp.saml.extensions.extensions.impl;

import org.opensaml.core.xml.XMLObject;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;

public class StringBasedMdsAttributeValueUnmarshaller extends BaseMdsSamlObjectUnmarshaller {

    public StringBasedMdsAttributeValueUnmarshaller() {
    }

    @Override
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        StringBasedMdsAttributeValue stringBasedMdsAttributeValue = (StringBasedMdsAttributeValue) samlObject;
        stringBasedMdsAttributeValue.setValue(elementContent);
    }
}
