package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.eidas.EncryptedAssertionKeys;

public class EncryptedAssertionKeysBuilder extends AbstractSAMLObjectBuilder<EncryptedAssertionKeys> {

    public EncryptedAssertionKeysBuilder() {
    }

    public EncryptedAssertionKeys buildObject() { return buildObject(EncryptedAssertionKeys.DEFAULT_ELEMENT_NAME, EncryptedAssertionKeys.TYPE_NAME); }

    @Override
    public EncryptedAssertionKeys buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new EncryptedAssertionKeysImpl(namespaceURI, localName, namespacePrefix);
    }
}