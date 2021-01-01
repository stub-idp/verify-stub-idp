package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.EidasGender;

public class EidasGenderMarshaller extends AbstractSAMLObjectMarshaller {

    public static final Marshaller MARSHALLER = new EidasGenderMarshaller();

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) {
        EidasGender eidasGender = (EidasGender) samlObject;
        ElementSupport.appendTextContent(domElement, eidasGender.getValue());
    }
}
