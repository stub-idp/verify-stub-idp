package stubidp.saml.extensions.extensions.eidas.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import stubidp.saml.extensions.extensions.eidas.EncryptedAssertionKeys;

import java.util.List;

public class EncryptedAssertionKeysImpl extends XSAnyImpl implements EncryptedAssertionKeys {
    private String encryptedAssertionKeys;

    protected EncryptedAssertionKeysImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public String getValue() {
        return encryptedAssertionKeys;
    }

    @Override
    public void setValue(String value) {
        encryptedAssertionKeys = prepareForAssignment(encryptedAssertionKeys, value);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}
