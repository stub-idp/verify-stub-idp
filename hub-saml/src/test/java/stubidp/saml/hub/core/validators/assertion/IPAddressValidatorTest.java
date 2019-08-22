package stubidp.saml.hub.core.validators.assertion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.test.OpenSAMLRunner;
import stubidp.saml.utils.core.test.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.utils.core.test.builders.IPAddressAttributeBuilder;

import static stubidp.saml.utils.core.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.utils.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static stubidp.saml.utils.core.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;

public class IPAddressValidatorTest extends OpenSAMLRunner {

    private IPAddressValidator validator;

    @BeforeEach
    public void setUp() throws Exception {
        validator = new IPAddressValidator();
    }

    @Test
    public void validate_shouldThrowWhenAssertionDoesNotContainAnAttributeStatement() throws Exception {
        Assertion assertion = anAssertion().buildUnencrypted();
        validateException(SamlTransformationErrorFactory.missingIPAddress(assertion.getID()), assertion);
    }

    @Test
    public void validate_shouldNotThrowWhenFirstAttributeStatementContainsAnIPAddressAttribute() throws Exception {
        Assertion assertion = anAssertion()
                .addAttributeStatement(anAttributeStatement().addAttribute(IPAddressAttributeBuilder.anIPAddress().build()).build())
                .buildUnencrypted();

        validator.validate(assertion);
    }

    @Test
    public void validate_shouldNotThrowWhenSecondAttributeStatementContainsAnIPAddressAttribute() throws Exception {
        Assertion assertion = anAssertion()
                .addAttributeStatement(anAttributeStatement().build())
                .addAttributeStatement(anAttributeStatement().addAttribute(IPAddressAttributeBuilder.anIPAddress().build()).build())
                .buildUnencrypted();

        validator.validate(assertion);
    }

    @Test
    public void validate_shouldNotThrowWhenFirstAttributeStatementContainsMultipleAttributesIncludingIPAddressAttribute() throws Exception {
        Assertion assertion = anAssertion()
                .addAttributeStatement(anAttributeStatement()
                        .addAttribute(aPersonName_1_1().buildAsFirstname())
                        .addAttribute(IPAddressAttributeBuilder.anIPAddress().build())
                        .build())
                .buildUnencrypted();

        validator.validate(assertion);
    }

    @Test
    public void validate_shouldThrowWhenAssertionContainsAttributeStatementsButNoIPAddressAttribute() throws Exception {
        Assertion assertion = anAssertion()
                .addAttributeStatement(anAttributeStatement().build())
                .buildUnencrypted();
        validateException(SamlTransformationErrorFactory.missingIPAddress(assertion.getID()), assertion);
    }

    @Test
    public void validate_shouldThrowWhenAssertionContainsIPAddressAttributeWithNoValue() throws Exception {
        Assertion assertion = anAssertion()
                .addAttributeStatement(anAttributeStatement().addAttribute(IPAddressAttributeBuilder.anIPAddress().withValue(null).build()).build())
                .buildUnencrypted();

        validateException(SamlTransformationErrorFactory.emptyIPAddress(assertion.getID()), assertion);
    }

    @Test
    public void validate_shouldNotWarnWhenAssertionContainsAnInvalidIPAddress() throws Exception {
        final String ipAddress = "10.10.10.1a";
        final Assertion assertion = anAssertion()
                .addAttributeStatement(anAttributeStatement().addAttribute(IPAddressAttributeBuilder.anIPAddress().withValue(ipAddress).build()).build())
                .buildUnencrypted();

        validator.validate(assertion);
    }

    private void validateException(SamlValidationSpecificationFailure failure, final Assertion assertion) {
        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(assertion),
                failure
        );
    }
}
