package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.Gpg45Status;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.util.Optional;

public class Gpg45StatusAttributeBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> value = Optional.of("IT01");

    private Gpg45StatusAttributeBuilder() {}

    public static Gpg45StatusAttributeBuilder aGpg45StatusAttribute() {
        return new Gpg45StatusAttributeBuilder();
    }

    public Attribute build() {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setName(IdaConstants.Attributes_1_1.GPG45Status.NAME);
        if (value.isPresent()){
            Gpg45Status attributeValue = openSamlXmlObjectFactory.createGpg45StatusAttributeValue(value.get());
            attribute.getAttributeValues().add(attributeValue);
        }

        return attribute;
    }

    public Gpg45StatusAttributeBuilder withValue(String value){
        this.value = Optional.ofNullable(value);
        return this;
    }
}
