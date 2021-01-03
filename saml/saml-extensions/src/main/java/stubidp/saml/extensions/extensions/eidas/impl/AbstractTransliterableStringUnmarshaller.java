package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import org.w3c.dom.Attr;
import stubidp.saml.extensions.extensions.eidas.TransliterableString;

abstract class AbstractTransliterableStringUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        TransliterableString transliterableString = (TransliterableString) samlObject;
        if (attribute.getLocalName().equals(TransliterableString.IS_LATIN_SCRIPT_ATTRIBUTE_NAME)) {
            transliterableString.setIsLatinScript(Boolean.valueOf(attribute.getValue()));
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
}
