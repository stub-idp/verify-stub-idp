package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;

public class CurrentGivenNameMarshaller extends AbstractTransliterableStringMarshaller {

    public static final Marshaller MARSHALLER = new CurrentGivenNameMarshaller();

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) {
        CurrentGivenName currentGivenName = (CurrentGivenName) samlObject;
        ElementSupport.appendTextContent(domElement, currentGivenName.getFirstName());
    }
}
