package stubidp.saml.hub.core.test.builders;

import org.opensaml.saml.saml2.core.AttributeValue;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

public class SimpleStringAttributeValueBuilder {

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private String value;

    public static SimpleStringAttributeValueBuilder aSimpleStringValue() {
        return new SimpleStringAttributeValueBuilder();
    }

    public AttributeValue build() {
        return openSamlXmlObjectFactory.createSimpleMdsAttributeValue(value);
    }

    public SimpleStringAttributeValueBuilder withValue(String value) {
        this.value = value;
        return this;
    }

}
