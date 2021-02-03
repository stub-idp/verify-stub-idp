package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.EncryptedAssertionKeys;

public class EncryptedAssertionKeysUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    public EncryptedAssertionKeysUnmarshaller() {
    }

    @Override
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        EncryptedAssertionKeys encryptedAssertionKeys = (EncryptedAssertionKeys) samlObject;
        encryptedAssertionKeys.setValue(elementContent);
    }
}