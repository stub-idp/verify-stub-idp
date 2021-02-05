package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.EncryptedAssertionKeys;

public class EncryptedAssertionKeysMarshaller extends AbstractSAMLObjectMarshaller {
    @Override
    protected void marshallElementContent(XMLObject samlObject, Element domElement) {
        EncryptedAssertionKeys encryptedAssertionKeys = (EncryptedAssertionKeys) samlObject;
        ElementSupport.appendTextContent(domElement, encryptedAssertionKeys.getValue());
    }
}