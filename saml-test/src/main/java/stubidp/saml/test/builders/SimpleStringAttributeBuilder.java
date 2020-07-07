package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.util.Optional;

public class SimpleStringAttributeBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> name = Optional.empty();
    private Optional<String> simpleStringValue = Optional.empty();

    private SimpleStringAttributeBuilder() {}

    public static SimpleStringAttributeBuilder aSimpleStringAttribute() {
        return new SimpleStringAttributeBuilder();
    }

    public Attribute build() {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();

        if (name.isPresent()) {
            attribute.setName(name.get());
        }
        if (simpleStringValue.isPresent()){
            StringBasedMdsAttributeValue attributeValue = openSamlXmlObjectFactory.createSimpleMdsAttributeValue(simpleStringValue.get());
            attribute.getAttributeValues().add(attributeValue);
        }

        return attribute;
    }

    public SimpleStringAttributeBuilder withName(String name) {
        this.name = Optional.ofNullable(name);
        return this;
    }

    public SimpleStringAttributeBuilder withSimpleStringValue(String value){
        this.simpleStringValue = Optional.ofNullable(value);
        return this;
    }
}
