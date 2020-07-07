package stubidp.saml.hub.hub.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.UserAccountCreationAttribute;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeQueryAttributeFactoryTest extends OpenSAMLRunner {

    private AttributeQueryAttributeFactory attributeQueryAttributeFactory;

    @BeforeEach
    public void setUp() throws Exception {
        attributeQueryAttributeFactory = new AttributeQueryAttributeFactory(new OpenSamlXmlObjectFactory());
    }

    @Test
    public void createAttribute_shouldPopulateAttributeNameFromUserAccountCreationAttributeValue(){
        UserAccountCreationAttribute userAccountCreationAttribute = UserAccountCreationAttribute.CURRENT_ADDRESS;

        Attribute attribute = attributeQueryAttributeFactory.createAttribute(userAccountCreationAttribute);

        assertThat(attribute.getName()).isEqualTo("currentaddress");
    }

    @Test
    public void createAttribute_shouldPopulateAttributeNameFormatWithUnspecifiedFormat(){
        UserAccountCreationAttribute userAccountCreationAttribute = UserAccountCreationAttribute.CURRENT_ADDRESS;

        Attribute attribute = attributeQueryAttributeFactory.createAttribute(userAccountCreationAttribute);

        assertThat(attribute.getNameFormat()).isEqualTo("urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified");
    }

    @Test
    public void createAttribute_shouldNotSetFriendlyName(){
        UserAccountCreationAttribute userAccountCreationAttribute = UserAccountCreationAttribute.CURRENT_ADDRESS;

        Attribute attribute = attributeQueryAttributeFactory.createAttribute(userAccountCreationAttribute);

        assertThat(attribute.getFriendlyName()).isNull();
    }
}
