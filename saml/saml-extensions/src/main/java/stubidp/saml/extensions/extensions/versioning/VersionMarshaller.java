package stubidp.saml.extensions.extensions.versioning;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;

public class VersionMarshaller extends AbstractSAMLObjectMarshaller {
    public static final Marshaller MARSHALLER = new VersionMarshaller();

    @Override
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) {
        XMLObjectSupport.marshallAttribute(XMLConstants.XSI_TYPE_ATTRIB_NAME, Version.NAMESPACE_PREFIX + ":" + Version.TYPE_LOCAL_NAME, domElement, false);
    }
}
