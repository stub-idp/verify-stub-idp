package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.eidas.EidasGender;

public class EidasGenderBuilder extends AbstractSAMLObjectBuilder<EidasGender> {

    /**
     * Constructor.
     */
    public EidasGenderBuilder() {

    }

    /** {@inheritDoc} */
    public EidasGender buildObject() {
        return buildObject(EidasGender.DEFAULT_ELEMENT_NAME, EidasGender.TYPE_NAME);
    }

    /** {@inheritDoc} */
    public EidasGender buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new EidasGenderImpl(namespaceURI, localName, namespacePrefix);
    }
}
