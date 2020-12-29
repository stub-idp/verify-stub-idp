package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class DateAttributeBuilder_1_1 {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private final List<AttributeValue> values = new ArrayList<>();
    private boolean addDefaultValue = true;
    private final AttributeValue defaultDateAttributeValue = DateAttributeValueBuilder.aDateValue().build();

    private DateAttributeBuilder_1_1() {}

    public static DateAttributeBuilder_1_1 aDate_1_1() {
        return new DateAttributeBuilder_1_1();
    }

    public Attribute buildAsDateOfBirth() {
        Attribute attribute = build();

        attribute.setFriendlyName(IdaConstants.Attributes_1_1.DateOfBirth.FRIENDLY_NAME);
        attribute.setName(IdaConstants.Attributes_1_1.DateOfBirth.NAME);
        attribute.setNameFormat(Attribute.UNSPECIFIED);

        return attribute;
    }

    public Attribute buildAsEidasDateOfBirth() {
        Attribute attribute = build();

        attribute.setFriendlyName(IdaConstants.Eidas_Attributes.DateOfBirth.FRIENDLY_NAME);
        attribute.setName(IdaConstants.Eidas_Attributes.DateOfBirth.NAME);
        attribute.setNameFormat(Attribute.UNSPECIFIED);

        return attribute;
    }

    public DateAttributeBuilder_1_1 addValue(AttributeValue attributeValue) {
        this.values.add(attributeValue);
        this.addDefaultValue = false;
        return this;
    }

    private Attribute build() {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setNameFormat(Attribute.UNSPECIFIED);
        if (addDefaultValue) {
            this.values.add(defaultDateAttributeValue);
        }

        for (AttributeValue value : values) {
            attribute.getAttributeValues().add(value);
        }
        return attribute;
    }
}
