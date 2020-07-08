package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;

public class DateOfBirthBuilder extends AbstractSAMLObjectBuilder<DateOfBirth> {

    /**
     * Constructor.
     */
    public DateOfBirthBuilder() {

    }

    /** {@inheritDoc} */
    public DateOfBirth buildObject() {
        return buildObject(DateOfBirth.DEFAULT_ELEMENT_NAME, DateOfBirth.TYPE_NAME);
    }

    /** {@inheritDoc} */
    public DateOfBirth buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new DateOfBirthImpl(namespaceURI, localName, namespacePrefix);
    }
}
