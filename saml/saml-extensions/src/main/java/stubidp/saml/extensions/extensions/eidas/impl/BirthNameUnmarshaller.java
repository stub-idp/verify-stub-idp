package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.BirthName;

public class BirthNameUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    public static final Unmarshaller UNMARSHALLER = new BirthNameUnmarshaller();

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        BirthName birthName = (BirthName) samlObject;
        birthName.setBirthName(elementContent);
    }
}
