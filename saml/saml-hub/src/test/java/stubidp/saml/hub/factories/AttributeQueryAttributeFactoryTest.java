package stubidp.saml.hub.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.UserAccountCreationAttribute;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeQueryAttributeFactoryTest extends OpenSAMLRunner {

    private AttributeQueryAttributeFactory attributeQueryAttributeFactory;

    @BeforeEach
    void setUp() {
        attributeQueryAttributeFactory = new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory());
    }

    @Test
    void createAttribute_shouldPopulateAttributeNameFromUserAccountCreationAttributeValue(){
        UserAccountCreationAttribute userAccountCreationAttribute = UserAccountCreationAttribute.CURRENT_ADDRESS;

        Attribute attribute = attributeQueryAttributeFactory.createAttribute(userAccountCreationAttribute);

        assertThat(attribute.getName()).isEqualTo("currentaddress");
    }

    @Test
    void createAttribute_shouldPopulateAttributeNameFormatWithUnspecifiedFormat(){
        UserAccountCreationAttribute userAccountCreationAttribute = UserAccountCreationAttribute.CURRENT_ADDRESS;

        Attribute attribute = attributeQueryAttributeFactory.createAttribute(userAccountCreationAttribute);

        assertThat(attribute.getNameFormat()).isEqualTo("urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified");
    }

    @Test
    void createAttribute_shouldNotSetFriendlyName(){
        UserAccountCreationAttribute userAccountCreationAttribute = UserAccountCreationAttribute.CURRENT_ADDRESS;

        Attribute attribute = attributeQueryAttributeFactory.createAttribute(userAccountCreationAttribute);

        assertThat(attribute.getFriendlyName()).isNull();
    }
}
