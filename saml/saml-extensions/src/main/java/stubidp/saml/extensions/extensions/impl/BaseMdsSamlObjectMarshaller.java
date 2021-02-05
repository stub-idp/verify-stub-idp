package stubidp.saml.extensions.extensions.impl;

import net.shibboleth.utilities.java.support.xml.NamespaceSupport;
import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.BaseMdsSamlObject;

import javax.xml.namespace.QName;

public class BaseMdsSamlObjectMarshaller extends AbstractSAMLObjectMarshaller {

    private final String xsiType;

    public BaseMdsSamlObjectMarshaller(){
        xsiType = null;
    }

    public BaseMdsSamlObjectMarshaller(String xsiType){
        this.xsiType = xsiType;
    }

    @Override
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        BaseMdsSamlObject simpleValueSamlObject = (BaseMdsSamlObject) xmlObject;

        if(xsiType != null){
            NamespaceSupport.appendNamespaceDeclaration(domElement, XMLConstants.XSI_NS, XMLConstants.XSI_PREFIX);
            XMLObjectSupport.marshallAttribute(XMLConstants.XSI_TYPE_ATTRIB_NAME, IdaConstants.IDA_PREFIX + ":" + xsiType, domElement, false);
            NamespaceSupport.appendNamespaceDeclaration(domElement, IdaConstants.IDA_NS, IdaConstants.IDA_PREFIX);
        }

        if (simpleValueSamlObject.getFrom() != null) {
            XMLObjectSupport.marshallAttribute(new QName(IdaConstants.IDA_NS, BaseMdsSamlObject.FROM_ATTRIB_NAME, IdaConstants.IDA_PREFIX), simpleValueSamlObject.getFrom().toString(), domElement, false);
        }
        if (simpleValueSamlObject.getTo() != null) {
            XMLObjectSupport.marshallAttribute(new QName(IdaConstants.IDA_NS, BaseMdsSamlObject.TO_ATTRIB_NAME, IdaConstants.IDA_PREFIX), simpleValueSamlObject.getTo().toString(), domElement, false);
        }
        XMLObjectSupport.marshallAttribute(new QName(IdaConstants.IDA_NS, BaseMdsSamlObject.VERIFIED_ATTRIB_NAME, IdaConstants.IDA_PREFIX), Boolean.toString(simpleValueSamlObject.getVerified()), domElement, false);

        super.marshallAttributes(xmlObject, domElement);
    }
}
