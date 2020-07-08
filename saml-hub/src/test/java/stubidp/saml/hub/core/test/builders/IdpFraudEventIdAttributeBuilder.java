package stubidp.saml.hub.core.test.builders;

import javax.xml.namespace.QName;

import org.opensaml.saml.saml2.core.Attribute;

import java.util.Optional;

import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.extensions.extensions.IdpFraudEventId;
import stubidp.saml.extensions.extensions.StringValueSamlObject;
import stubidp.saml.extensions.extensions.impl.StringBasedMdsAttributeValueBuilder;

public class IdpFraudEventIdAttributeBuilder {

    private static final java.lang.String INVALID_TYPE_LOCAL_NAME = "InvalidFraudEventType";
    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<String> value = Optional.ofNullable("default-event-id");

    public static IdpFraudEventIdAttributeBuilder anIdpFraudEventIdAttribute() {
        return new IdpFraudEventIdAttributeBuilder();
    }

    public Attribute build() {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setName(IdaConstants.Attributes_1_1.IdpFraudEventId.NAME);
        if (value.isPresent()){
            IdpFraudEventId attributeValue = openSamlXmlObjectFactory.createIdpFraudEventAttributeValue(value.get());
            attribute.getAttributeValues().add(attributeValue);
        }

        return attribute;
    }

    public Attribute buildInvalidAttribute() {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setName(IdaConstants.Attributes_1_1.IdpFraudEventId.NAME);
        if (value.isPresent()){
            QName typeName = new QName(IdaConstants.IDA_NS, INVALID_TYPE_LOCAL_NAME, IdaConstants.IDA_PREFIX);
            StringValueSamlObject idpFraudEventId = new StringBasedMdsAttributeValueBuilder().buildObject(IdpFraudEventId.DEFAULT_ELEMENT_NAME, typeName);
            idpFraudEventId.setValue(value.get());
            attribute.getAttributeValues().add(idpFraudEventId);
        }

        return attribute;
    }

    public IdpFraudEventIdAttributeBuilder withValue(String value){
        this.value = Optional.ofNullable(value);
        return this;
    }
}
