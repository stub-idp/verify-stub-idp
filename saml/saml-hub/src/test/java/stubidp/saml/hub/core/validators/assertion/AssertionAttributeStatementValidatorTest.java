package stubidp.saml.hub.core.validators.assertion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.extensions.extensions.PersonName;
import stubidp.saml.hub.core.test.builders.IdpFraudEventIdAttributeBuilder;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.test.builders.AssertionBuilder;

import static java.util.Arrays.asList;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.invalidAttributeLanguageInAssertion;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.invalidFraudAttribute;
import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.test.builders.SimpleStringAttributeBuilder.aSimpleStringAttribute;

@ExtendWith(MockitoExtension.class)
public class AssertionAttributeStatementValidatorTest extends OpenSAMLRunner {

    private AssertionAttributeStatementValidator validator;

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    @BeforeEach
    public void setUp() {
        validator = new AssertionAttributeStatementValidator();
    }

    @Test
    public void validate_shouldThrowWhenAttributesIsNotEnGb() {
        Attribute validAttributeOne = aSimpleStringAttribute().build();
        PersonName validFirstNameAttribute = openSamlXmlObjectFactory.createPersonNameAttributeValue("Dave");
        validAttributeOne.getAttributeValues().add(validFirstNameAttribute);

        Attribute validAttributeTwo = aSimpleStringAttribute().build();
        PersonName validSurnameAttributeValue = openSamlXmlObjectFactory.createPersonNameAttributeValue("Jones");
        validAttributeTwo.getAttributeValues().add(validSurnameAttributeValue);

        Attribute invalidMiddlenameAttribute = aSimpleStringAttribute().build();
        PersonName invalidMiddlenameAttributeValue = openSamlXmlObjectFactory.createPersonNameAttributeValue("Middle");
        invalidMiddlenameAttributeValue.setLanguage("en-US");
        invalidMiddlenameAttribute.getAttributeValues().add(invalidMiddlenameAttributeValue);

        final Assertion assertion = AssertionBuilder.anAssertion()
                .addAttributeStatement(anAttributeStatement().build())
                .addAttributeStatement(anAttributeStatement().build())
                .buildUnencrypted();

        assertion.getAttributeStatements().get(0).getAttributes().add(validAttributeOne);
        assertion.getAttributeStatements().get(1).getAttributes().addAll(asList(validAttributeTwo, invalidMiddlenameAttribute));

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(assertion),
                invalidAttributeLanguageInAssertion(invalidMiddlenameAttribute.getName(), invalidMiddlenameAttributeValue.getLanguage())
        );
    }

    @Test
    public void validate_shouldThrowWhenFraudEventNotCorrect() {
        final Assertion assertion = AssertionBuilder.anAssertion()
                .addAttributeStatement(anAttributeStatement().addAttribute(IdpFraudEventIdAttributeBuilder.anIdpFraudEventIdAttribute().buildInvalidAttribute()).build())
                .buildUnencrypted();

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validateFraudEvent(assertion),
                invalidFraudAttribute(AssertionAttributeStatementValidator.INVALID_FRAUD_EVENT_TYPE)
        );

    }

}
