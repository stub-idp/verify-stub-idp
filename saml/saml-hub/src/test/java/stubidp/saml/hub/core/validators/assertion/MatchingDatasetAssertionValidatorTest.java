package stubidp.saml.hub.core.validators.assertion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.Date;
import stubidp.saml.extensions.extensions.PersonName;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;
import stubidp.saml.test.OpenSAMLRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.attributeStatementEmpty;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.attributeWithIncorrectType;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.emptyAttribute;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.mdsAttributeNotRecognised;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.mdsMultipleStatements;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.mdsStatementMissing;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.MatchingDatasetAttributeStatementBuilder_1_1.aMatchingDatasetAttributeStatement_1_1;
import static stubidp.saml.test.builders.MatchingDatasetAttributeStatementBuilder_1_1.anEmptyMatchingDatasetAttributeStatement_1_1;
import static stubidp.saml.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static stubidp.saml.test.builders.PersonNameAttributeValueBuilder.aPersonNameValue;
import static stubidp.saml.test.builders.SimpleStringAttributeBuilder.aSimpleStringAttribute;
import static stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper.validateFail;

@ExtendWith(MockitoExtension.class)
public class MatchingDatasetAssertionValidatorTest extends OpenSAMLRunner {

    private static final String RESPONSE_ISSUER_ID = "issuer ID";

    @Mock
    private DuplicateAssertionValidator duplicateAssertionValidator;

    private MatchingDatasetAssertionValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new MatchingDatasetAssertionValidator(duplicateAssertionValidator);
    }

    @Test
    public void validate_shouldThrowExceptionWhenNameIsNotRecognised() {
        Attribute attribute = aSimpleStringAttribute().withName("dummy attribute").build();
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().addCustomAttribute(attribute).build();
        Assertion assertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();

        validateFail(
            () -> validator.validate(assertion, RESPONSE_ISSUER_ID),
            mdsAttributeNotRecognised("dummy attribute")
        );
    }

    @Test
    public void validate_shouldNotThrowAnExceptionWhenFirstnameIsPresent_ProfileV1_1() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().withFirstname(aPersonName_1_1().buildAsFirstname()).build();
        Assertion matchingDatasetAssertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldNotThrowAnExceptionWhenMiddleNameIsPresent_ProfileV1_1() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().withMiddleNames(aPersonName_1_1().buildAsMiddlename()).build();
        Assertion matchingDatasetAssertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldNotThrowAnExceptionWhenSurNameIsPresent_ProfileV1_1() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().withSurname(aPersonName_1_1().buildAsSurname()).build();
        Assertion matchingDatasetAssertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldNotThrowAnExceptionWhenDateOfBirthIsPresent_ProfileV1_1() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().withDateOfBirth().build();
        Assertion matchingDatasetAssertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldNotThrowAnExceptionWhenGenderIsPresent_ProfileV1_1() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().withGender().build();
        Assertion matchingDatasetAssertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldNotThrowAnExceptionWhenCurrentAddressIsPresent_ProfileV1_1() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().withCurrentAddress().build();
        Assertion matchingDatasetAssertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldNotThrowAnExceptionWhenPreviousAddressIsPresent_ProfileV1_1() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().addPreviousAddress().build();
        Assertion matchingDatasetAssertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldThrowExceptionWhenNoAttributesArePresent() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().build();
        Assertion matchingDatasetAssertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();

        validateFail(
            () -> validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID),
            attributeStatementEmpty(matchingDatasetAssertion.getID())
        );
    }

    @Test
    public void validate_shouldThrowExceptionWhenNoAttributeStatementsArePresent() {
        Assertion matchingDatasetAssertion = anAssertion().buildUnencrypted();

        validateFail(
            () -> validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID),
            mdsStatementMissing()
        );
    }

    @Test
    public void validate_shouldThrowExceptionWhenMultipleAttributeStatementsArePresent() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().build();
        Assertion matchingDatasetAssertion = anAssertion()
                .addAttributeStatement(attributeStatement)
                .addAttributeStatement(attributeStatement)
                .buildUnencrypted();

        validateFail(
            () -> validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID),
            mdsMultipleStatements()
        );
    }

    @Test
    public void validate_shouldThrowExceptionWhenAttributeIsMissingValue() {
        Attribute attribute = aPersonName_1_1()
                .buildAsFirstnameWithNoAttributeValues();
        AttributeStatement attributeStatement = aMatchingDatasetAttributeStatement_1_1()
                .withFirstname(attribute)
                .build();
        Assertion matchingDatasetAssertion = anAssertion()
                .addAttributeStatement(attributeStatement)
                .buildUnencrypted();

        validateFail(
            () -> validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID),
            emptyAttribute("MDS_firstname")
        );
    }

    @Test
    public void validate_shouldThrowExceptionWhenAttributeValueIsIncorrectType() {
        Attribute attribute = aSimpleStringAttribute().withName(IdaConstants.Attributes_1_1.Firstname.NAME).withSimpleStringValue("Joe").build();
        AttributeStatement attributeStatement = aMatchingDatasetAttributeStatement_1_1()
                .withFirstname(attribute)
                .build();
        Assertion matchingDatasetAssertion = anAssertion()
                .addAttributeStatement(attributeStatement)
                .buildUnencrypted();

        validateFail(
            () -> validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID),
            attributeWithIncorrectType(IdaConstants.Attributes_1_1.Firstname.NAME, PersonName.TYPE_NAME, StringBasedMdsAttributeValue.TYPE_NAME)
        );
    }

    @Test
    public void validate_shouldNotThrowExceptionWhenAttributeValueVerifiedIsAbsent() {
        Attribute attribute = aPersonName_1_1().addValue(aPersonNameValue().withVerified(null).build()).buildAsFirstname();
        AttributeStatement attributeStatement = aMatchingDatasetAttributeStatement_1_1()
                .withFirstname(attribute)
                .build();
        Assertion matchingDatasetAssertion = anAssertion()
                .addAttributeStatement(attributeStatement)
                .buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldNotThrowExceptionWhenAttributeValueToDateIsAbsent() {
        Attribute attribute = aPersonName_1_1().addValue(aPersonNameValue().withTo(null).build()).buildAsFirstname();
        AttributeStatement attributeStatement = aMatchingDatasetAttributeStatement_1_1()
                .withFirstname(attribute)
                .build();
        Assertion matchingDatasetAssertion = anAssertion()
                .addAttributeStatement(attributeStatement)
                .buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldNotThrowExceptionWhenAttributeValueFromDateIsAbsent() {
        Attribute attribute = aPersonName_1_1().addValue(aPersonNameValue().withFrom(null).build()).buildAsFirstname();
        AttributeStatement attributeStatement = aMatchingDatasetAttributeStatement_1_1()
                .withFirstname(attribute)
                .build();
        Assertion matchingDatasetAssertion = anAssertion()
                .addAttributeStatement(attributeStatement)
                .buildUnencrypted();

        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }

    @Test
    public void validate_shouldThrowExceptionWhenAttributeValueTypeIsValidButIncorrectForAttribute() {
        Attribute attribute = aPersonName_1_1().addValue(aPersonNameValue().withFrom(null).build()).buildAsFirstname();
        attribute.setName(IdaConstants.Attributes_1_1.DateOfBirth.NAME);
        AttributeStatement attributeStatement = aMatchingDatasetAttributeStatement_1_1()
                .withDateOfBirth(attribute)
                .build();
        Assertion matchingDatasetAssertion = anAssertion()
                .addAttributeStatement(attributeStatement)
                .buildUnencrypted();

        validateFail(
            () -> validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID),
            attributeWithIncorrectType(IdaConstants.Attributes_1_1.DateOfBirth.NAME, Date.TYPE_NAME, PersonName.TYPE_NAME)
        );
    }

    @Test
    public void validate_shouldValidateForDuplicateIds() {
        AttributeStatement attributeStatement = anEmptyMatchingDatasetAttributeStatement_1_1().withFirstname(aPersonName_1_1().buildAsFirstname()).build();
        Assertion matchingDatasetAssertion = anAssertion().addAttributeStatement(attributeStatement).buildUnencrypted();
        validator.validate(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
        verify(duplicateAssertionValidator, times(1)).validateMatchingDataSetAssertion(matchingDatasetAssertion, RESPONSE_ISSUER_ID);
    }
}
