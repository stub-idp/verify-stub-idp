package stubidp.saml.extensions.extensions.impl;


import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.w3c.dom.Element;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.LocalisableAttributeValue;
import stubidp.saml.extensions.extensions.PersonName;

import javax.xml.namespace.QName;

public class LocalisableStringBasedMdsAttributeValueMarshaller extends StringBasedMdsAttributeValueMarshaller {

    public LocalisableStringBasedMdsAttributeValueMarshaller(String xsiType){
        super(xsiType);
    }

    @Override
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        LocalisableAttributeValue localisableAttributeValue = (LocalisableAttributeValue) xmlObject;

        String language = localisableAttributeValue.getLanguage();
        if (language != null) {
            XMLObjectSupport.marshallAttribute(new QName(IdaConstants.IDA_NS, PersonName.LANGUAGE_ATTRIB_NAME, IdaConstants.IDA_PREFIX), localisableAttributeValue.getLanguage(), domElement, false);
        }

        super.marshallAttributes(xmlObject, domElement);
    }
}
