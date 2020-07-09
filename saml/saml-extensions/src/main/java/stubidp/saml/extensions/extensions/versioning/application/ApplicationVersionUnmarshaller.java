package stubidp.saml.extensions.extensions.versioning.application;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;

public class ApplicationVersionUnmarshaller extends AbstractSAMLObjectUnmarshaller {
    public static final Unmarshaller UNMARSHALLER = new ApplicationVersionUnmarshaller();

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        ApplicationVersion applicationVersion = (ApplicationVersion) samlObject;
        applicationVersion.setValue(elementContent);
    }
}

