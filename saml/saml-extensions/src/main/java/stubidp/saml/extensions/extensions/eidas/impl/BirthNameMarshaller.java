package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.BirthName;

public class BirthNameMarshaller extends AbstractSAMLObjectMarshaller {

    public static final Marshaller MARSHALLER = new BirthNameMarshaller();

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) {
        BirthName birthName = (BirthName) samlObject;
        ElementSupport.appendTextContent(domElement, birthName.getBirthName());
    }
}
