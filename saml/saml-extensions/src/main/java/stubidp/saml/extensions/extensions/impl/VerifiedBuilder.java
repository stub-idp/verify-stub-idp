package stubidp.saml.extensions.extensions.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.Verified;

public class VerifiedBuilder extends AbstractSAMLObjectBuilder<Verified> {

    public VerifiedBuilder() {
    }

    @Override
    public Verified buildObject() {
        return buildObject(Verified.DEFAULT_ELEMENT_NAME, Verified.TYPE_NAME);
    }

    @Override
    public Verified buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new VerifiedImpl(namespaceURI, localName, namespacePrefix, Verified.TYPE_NAME);
    }
}
