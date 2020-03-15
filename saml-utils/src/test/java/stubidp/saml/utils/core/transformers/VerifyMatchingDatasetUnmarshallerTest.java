package stubidp.saml.utils.core.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import stubidp.saml.extensions.extensions.Address;
import stubidp.saml.extensions.extensions.Date;
import stubidp.saml.extensions.extensions.Gender;
import stubidp.saml.extensions.extensions.PersonName;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.utils.OpenSAMLRunner;
import stubidp.saml.utils.core.domain.AddressFactory;
import stubidp.saml.utils.core.domain.MatchingDataset;
import stubidp.saml.utils.core.test.builders.AddressAttributeBuilder_1_1;
import stubidp.saml.utils.core.test.builders.AddressAttributeValueBuilder_1_1;
import stubidp.saml.utils.core.test.builders.AssertionBuilder;
import stubidp.saml.utils.core.test.builders.AttributeStatementBuilder;
import stubidp.saml.utils.core.test.builders.DateAttributeBuilder_1_1;
import stubidp.saml.utils.core.test.builders.DateAttributeValueBuilder;
import stubidp.saml.utils.core.test.builders.GenderAttributeBuilder_1_1;
import stubidp.saml.utils.core.test.builders.NameIdBuilder;
import stubidp.saml.utils.core.test.builders.PersonNameAttributeBuilder_1_1;
import stubidp.saml.utils.core.test.builders.PersonNameAttributeValueBuilder;
import stubidp.saml.utils.core.test.builders.SubjectBuilder;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class VerifyMatchingDatasetUnmarshallerTest extends OpenSAMLRunner {

    private VerifyMatchingDatasetUnmarshaller unmarshaller;

    @BeforeEach
    public void setUp() {
        this.unmarshaller = new VerifyMatchingDatasetUnmarshaller(new AddressFactory());
    }

    @Test
    public void transform_shouldTransformAnAssertionIntoAMatchingDataset_1_1() {
        Attribute firstname = PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Bob").withFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-03-05")).withTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2001-02-06")).withVerified(true).build()).buildAsFirstname();
        Attribute middlenames = PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withValue("foo").withFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-03-05")).withTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2001-02-06")).withVerified(true).build()).buildAsMiddlename();
        Attribute surname = PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Bobbins").withFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-03-05")).withTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2001-02-06")).withVerified(true).build()).buildAsSurname();
        Attribute gender = GenderAttributeBuilder_1_1.aGender_1_1().withValue("Male").withFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2000-03-05")).withTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2001-02-06")).withVerified(true).build();
        Attribute dateOfBirth = DateAttributeBuilder_1_1.aDate_1_1().addValue(DateAttributeValueBuilder.aDateValue().withValue("1986-12-05").withFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2001-09-08")).withTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2002-03-05")).withVerified(false).build()).buildAsDateOfBirth();
        Address address = AddressAttributeValueBuilder_1_1.anAddressAttributeValue().addLines(Collections.singletonList("address-line-1")).withFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2012-08-08")).withTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2012-09-09")).build();
        Attribute currentAddress = AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(address).buildCurrentAddress();

        Address previousAddress1 = AddressAttributeValueBuilder_1_1.anAddressAttributeValue().addLines(Collections.singletonList("address-line-2")).withFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2011-08-08")).withTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2012-08-07")).build();
        Address previousAddress2 = AddressAttributeValueBuilder_1_1.anAddressAttributeValue().addLines(Collections.singletonList("address-line-3")).withFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2010-08-08")).withTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2011-08-07")).build();
        Attribute previousAddresses = AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(previousAddress1).addAddress(previousAddress2).buildPreviousAddress();

        String pid = "PID12345";
        Assertion originalAssertion = AssertionBuilder.aMatchingDatasetAssertion(firstname, middlenames, surname, gender, dateOfBirth, currentAddress, previousAddresses)
                .withSubject(SubjectBuilder.aSubject().withNameId(NameIdBuilder.aNameId().withValue(pid).build()).build())
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(originalAssertion);

        final PersonName firstNameAttributeValue = (PersonName) firstname.getAttributeValues().get(0);
        final PersonName middleNameAttributeValue = (PersonName) middlenames.getAttributeValues().get(0);
        final PersonName surnameAttributeValue = (PersonName) surname.getAttributeValues().get(0);
        final Gender genderAttributeValue = (Gender) gender.getAttributeValues().get(0);
        final Date dateOfBirthAttributeValue = (Date) dateOfBirth.getAttributeValues().get(0);
        final Address currentAddressAttributeValue = (Address) currentAddress.getAttributeValues().get(0);
        assertThat(matchingDataset.getFirstNames().get(0).getValue()).isEqualTo(firstNameAttributeValue.getValue());
        assertThat(matchingDataset.getFirstNames().get(0).getFrom()).isEqualTo(firstNameAttributeValue.getFrom());
        assertThat(matchingDataset.getFirstNames().get(0).getTo()).isEqualTo(firstNameAttributeValue.getTo());
        assertThat(matchingDataset.getMiddleNames().get(0).getValue()).isEqualTo(middleNameAttributeValue.getValue());
        assertThat(matchingDataset.getMiddleNames().get(0).getFrom()).isEqualTo(middleNameAttributeValue.getFrom());
        assertThat(matchingDataset.getMiddleNames().get(0).getTo()).isEqualTo(middleNameAttributeValue.getTo());
        assertThat(matchingDataset.getSurnames().get(0).getValue()).isEqualTo(surnameAttributeValue.getValue());
        assertThat(matchingDataset.getSurnames().get(0).getFrom()).isEqualTo(surnameAttributeValue.getFrom());
        assertThat(matchingDataset.getSurnames().get(0).getTo()).isEqualTo(surnameAttributeValue.getTo());

        assertThat(matchingDataset.getGender().get().getValue().getValue()).isEqualTo(genderAttributeValue.getValue());
        assertThat(matchingDataset.getGender().get().getFrom()).isEqualTo(genderAttributeValue.getFrom());
        assertThat(matchingDataset.getGender().get().getTo()).isEqualTo(genderAttributeValue.getTo());

        assertThat(matchingDataset.getDateOfBirths().get(0).getValue()).isEqualTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of(dateOfBirthAttributeValue.getValue()));
        assertThat(matchingDataset.getDateOfBirths().get(0).getFrom()).isEqualTo(dateOfBirthAttributeValue.getFrom());
        assertThat(matchingDataset.getDateOfBirths().get(0).getTo()).isEqualTo(dateOfBirthAttributeValue.getTo());

        assertThat(matchingDataset.getAddresses().size()).isEqualTo(3);

        stubidp.saml.utils.core.domain.Address transformedCurrentAddress = matchingDataset.getAddresses().get(0);
        assertThat(transformedCurrentAddress.getLines().get(0)).isEqualTo(currentAddressAttributeValue.getLines().get(0).getValue());
        assertThat(transformedCurrentAddress.getPostCode().get()).isEqualTo(currentAddressAttributeValue.getPostCode().getValue());
        assertThat(transformedCurrentAddress.getInternationalPostCode().get()).isEqualTo(currentAddressAttributeValue.getInternationalPostCode().getValue());
        assertThat(transformedCurrentAddress.getUPRN().get()).isEqualTo(currentAddressAttributeValue.getUPRN().getValue());
        assertThat(transformedCurrentAddress.getFrom()).isEqualTo(currentAddressAttributeValue.getFrom());
        assertThat(transformedCurrentAddress.getTo().get()).isEqualTo(currentAddressAttributeValue.getTo());

        stubidp.saml.utils.core.domain.Address transformedPreviousAddress1 = matchingDataset.getAddresses().get(1);
        assertThat(transformedPreviousAddress1.getLines().get(0)).isEqualTo(previousAddress1.getLines().get(0).getValue());
        stubidp.saml.utils.core.domain.Address transformedPreviousAddress2 = matchingDataset.getAddresses().get(2);
        assertThat(transformedPreviousAddress2.getLines().get(0)).isEqualTo(previousAddress2.getLines().get(0).getValue());

        assertThat(matchingDataset.getPersonalId()).isEqualTo(pid);
    }

    @Test
    public void transform_shoulHandleWhenMatchingDatasetIsPresentAndToDateIsMissingFromCurrentAddress() {
        Attribute currentAddress = AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().withTo(null).build()).buildCurrentAddress();
        Assertion assertion = AssertionBuilder.aMatchingDatasetAssertion(
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsFirstname(),
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsMiddlename(),
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsSurname(),
                GenderAttributeBuilder_1_1.aGender_1_1().build(),
                DateAttributeBuilder_1_1.aDate_1_1().buildAsDateOfBirth(),
                currentAddress,
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildPreviousAddress())
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(assertion);

        assertThat(matchingDataset).isNotNull();
    }

    @Test
    public void transform_shoulHandleWhenMatchingDatasetIsPresentAndToDateIsMissingFromPreviousAddress() {
        Assertion assertion = AssertionBuilder.aMatchingDatasetAssertion(
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsFirstname(),
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsMiddlename(),
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsSurname(),
                GenderAttributeBuilder_1_1.aGender_1_1().build(),
                DateAttributeBuilder_1_1.aDate_1_1().buildAsDateOfBirth(),
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildCurrentAddress(),
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().withTo(null).build()).buildPreviousAddress()).buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(assertion);

        assertThat(matchingDataset).isNotNull();
    }

    @Test
    public void transform_shoulHandleWhenMatchingDatasetIsPresentAndToDateIsMissingFromFirstName() {
        Attribute firstName = PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withTo(null).build()).buildAsFirstname();
        Assertion assertion = AssertionBuilder.aMatchingDatasetAssertion(
                firstName,
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsMiddlename(),
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsSurname(),
                GenderAttributeBuilder_1_1.aGender_1_1().build(),
                DateAttributeBuilder_1_1.aDate_1_1().buildAsDateOfBirth(),
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildCurrentAddress(),
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildPreviousAddress())
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(assertion);

        assertThat(matchingDataset).isNotNull();
    }

    @Test
    public void transform_shoulHandleWhenMatchingDatasetIsPresentAndToDateIsPresentInFirstName() {
        Attribute firstName = PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1066-01-05")).build()).buildAsFirstname();
        Assertion assertion = AssertionBuilder.aMatchingDatasetAssertion(
                firstName,
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsMiddlename(),
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsSurname(),
                GenderAttributeBuilder_1_1.aGender_1_1().build(),
                DateAttributeBuilder_1_1.aDate_1_1().buildAsDateOfBirth(),
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildCurrentAddress(),
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildPreviousAddress())
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(assertion);

        assertThat(matchingDataset).isNotNull();
    }

    @Test
    public void transform_shouldMapMultipleFirstNames() {
        Attribute firstName = PersonNameAttributeBuilder_1_1.aPersonName_1_1()
                .addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withValue("name1").build())
                .addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withValue("name2").build())
                .buildAsFirstname();

        AttributeStatement attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement().addAttribute(firstName).build();
        Assertion matchingDatasetAssertion = AssertionBuilder.anAssertion()
                .addAttributeStatement(attributeStatementBuilder)
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(matchingDatasetAssertion);

        assertThat(matchingDataset).isNotNull();
        assertThat(matchingDataset.getFirstNames().size()).isEqualTo(2);
    }
    @Test
    public void transform_shouldMapMultipleSurnames() {
        Attribute surName = PersonNameAttributeBuilder_1_1.aPersonName_1_1()
                .addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withValue("name1").build())
                .addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withValue("name2").build())
                .buildAsSurname();

        AttributeStatement attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement().addAttribute(surName).build();
        Assertion matchingDatasetAssertion = AssertionBuilder.anAssertion()
                .addAttributeStatement(attributeStatementBuilder)
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(matchingDatasetAssertion);

        assertThat(matchingDataset).isNotNull();
        assertThat(matchingDataset.getSurnames().size()).isEqualTo(2);
    }

    @Test
    public void transform_shouldMapMultipleMiddleNames() {
        Attribute middleName = PersonNameAttributeBuilder_1_1.aPersonName_1_1()
                .addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withValue("name1").build())
                .addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withValue("name2").build())
                .buildAsMiddlename();

        AttributeStatement attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement().addAttribute(middleName).build();
        Assertion matchingDatasetAssertion = AssertionBuilder.anAssertion()
                .addAttributeStatement(attributeStatementBuilder)
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(matchingDatasetAssertion);

        assertThat(matchingDataset).isNotNull();
        assertThat(matchingDataset.getMiddleNames().size()).isEqualTo(2);
    }

    @Test
    public void transform_shouldMapMultipleBirthdates() {
        Attribute attribute = DateAttributeBuilder_1_1.aDate_1_1().addValue(DateAttributeValueBuilder.aDateValue().withValue("2012-12-12").build()).addValue(DateAttributeValueBuilder.aDateValue().withValue("2011-12-12").build()).buildAsDateOfBirth();

        AttributeStatement attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement().addAttribute(attribute).build();
        Assertion matchingDatasetAssertion = AssertionBuilder.anAssertion()
                .addAttributeStatement(attributeStatementBuilder)
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(matchingDatasetAssertion);

        assertThat(matchingDataset).isNotNull();
        assertThat(matchingDataset.getDateOfBirths().size()).isEqualTo(2);
    }

    @Test
    public void transform_shouldMapMultipleCurrentAddresses() {
        Attribute attribute = AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildCurrentAddress();

        AttributeStatement attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement().addAttribute(attribute).build();
        Assertion matchingDatasetAssertion = AssertionBuilder.anAssertion()
                .addAttributeStatement(attributeStatementBuilder)
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(matchingDatasetAssertion);

        assertThat(matchingDataset).isNotNull();
        assertThat(matchingDataset.getAddresses().size()).isEqualTo(2);
    }

    @Test
    public void transform_shouldMapMultiplePreviousAddresses() {
        Attribute attribute = AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildPreviousAddress();

        AttributeStatement attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement().addAttribute(attribute).build();
        Assertion matchingDatasetAssertion = AssertionBuilder.anAssertion()
                .addAttributeStatement(attributeStatementBuilder)
                .buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(matchingDatasetAssertion);

        assertThat(matchingDataset).isNotNull();
        assertThat(matchingDataset.getAddresses().size()).isEqualTo(2);
    }

}
