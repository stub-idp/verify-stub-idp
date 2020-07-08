package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.AttributeValue;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

public class VerifiedAttributeValueBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private boolean value;

    private VerifiedAttributeValueBuilder() {}

    public static VerifiedAttributeValueBuilder aVerifiedValue() {
        return new VerifiedAttributeValueBuilder();
    }

    public AttributeValue build() {
        return openSamlXmlObjectFactory.createVerifiedAttributeValue(value);
    }

    public VerifiedAttributeValueBuilder withValue(boolean value) {
        this.value = value;
        return this;
    }
}
