package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;

public class CurrentFamilyNameBuilder extends AbstractSAMLObjectBuilder<CurrentFamilyName> {

    /**
     * Constructor.
     */
    public CurrentFamilyNameBuilder() {

    }

    /** {@inheritDoc} */
    public CurrentFamilyName buildObject() {
        return buildObject(CurrentFamilyName.DEFAULT_ELEMENT_NAME, CurrentFamilyName.TYPE_NAME);
    }

    /** {@inheritDoc} */
    public CurrentFamilyName buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new CurrentFamilyNameImpl(namespaceURI, localName, namespacePrefix);
    }
}
