package stubidp.saml.extensions.extensions.impl;


import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.Gender;

public class GenderBuilder extends AbstractSAMLObjectBuilder<Gender> {

    @Override
    public Gender buildObject() {
        return buildObject(Gender.DEFAULT_ELEMENT_NAME, Gender.TYPE_NAME);
    }

    @Override
    public Gender buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new GenderImpl(namespaceURI, localName, namespacePrefix, Gender.TYPE_NAME);
    }
}
