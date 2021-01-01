package stubidp.saml.extensions.extensions.versioning.application;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;

public class ApplicationVersionMarshaller extends AbstractSAMLObjectMarshaller {
    public static final Marshaller MARSHALLER = new ApplicationVersionMarshaller();

    /**
     * {@inheritDoc}
     */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) {
        ApplicationVersion applicationVersion = (ApplicationVersion) samlObject;
        ElementSupport.appendTextContent(domElement, applicationVersion.getValue());
    }
}
