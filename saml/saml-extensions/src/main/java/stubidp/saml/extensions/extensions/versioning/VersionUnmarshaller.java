package stubidp.saml.extensions.extensions.versioning;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import stubidp.saml.extensions.extensions.versioning.application.ApplicationVersion;

public class VersionUnmarshaller extends AbstractSAMLObjectUnmarshaller {
    public static final Unmarshaller UNMARSHALLER = new VersionUnmarshaller();

    protected void processChildElement(XMLObject parentObject, XMLObject childObject) {
        Version version = (Version) parentObject;
        version.setApplicationVersion((ApplicationVersion) childObject);
    }
}
