package stubidp.saml.extensions.extensions.impl;


import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.PersonName;

public class PersonNameBuilder extends AbstractSAMLObjectBuilder<PersonName> {

    public PersonNameBuilder() {
    }

    @Override
    public PersonName buildObject() {
        return buildObject(PersonName.DEFAULT_ELEMENT_NAME, PersonName.TYPE_NAME);
    }

    @Override
    public PersonName buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new PersonNameImpl(namespaceURI, localName, namespacePrefix, PersonName.TYPE_NAME);
    }
}
