package stubidp.saml.utils.core.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.eidas.EidasGender;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentFamilyNameBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentGivenNameBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.DateOfBirthBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.EidasGenderBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.PersonIdentifierBuilder;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.utils.OpenSAMLRunner;
import stubidp.saml.utils.core.domain.Gender;
import stubidp.saml.utils.core.domain.MatchingDataset;
import stubidp.saml.utils.core.test.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.test.builders.AssertionBuilder;
import stubidp.saml.utils.core.test.builders.PersonIdentifierAttributeBuilder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class EidasMatchingDatasetUnmarshallerTest extends OpenSAMLRunner {

    private EidasMatchingDatasetUnmarshaller unmarshaller;
    private static OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    @BeforeEach
    public void setUp() {
        this.unmarshaller = new EidasMatchingDatasetUnmarshaller();
    }

    @Test
    public void transformShouldTransformAnAssertionIntoAMatchingDataset() {
        Attribute firstname = anEidasFirstName("Bob", true);
        Attribute surname = anEidasFamilyName("Bobbins", true);
        Instant dob = BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1986-12-05");
        Attribute dateOfBirth = anEidasDateOfBirth(dob);

        PersonIdentifier personIdentifier = new PersonIdentifierBuilder().buildObject();
        personIdentifier.setPersonIdentifier("PID12345");
        Attribute personalIdentifier = PersonIdentifierAttributeBuilder.aPersonIdentifier().withValue(personIdentifier).build();
        Assertion originalAssertion = AssertionBuilder.anEidasMatchingDatasetAssertion(firstname, surname, dateOfBirth, personalIdentifier,
                Optional.empty()).buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(originalAssertion);

        assertThat(matchingDataset.getFirstNames().get(0).getValue()).isEqualTo("Bob");
        assertThat(matchingDataset.getSurnames().get(0).getValue()).isEqualTo("Bobbins");
        assertThat(matchingDataset.getDateOfBirths().get(0).getValue()).isEqualTo(dob);
        assertThat(matchingDataset.getPersonalId()).isEqualTo("PID12345");

        assertThat(matchingDataset.getFirstNames().get(0).isVerified()).isTrue();
        assertThat(matchingDataset.getSurnames().get(0).isVerified()).isTrue();
        assertThat(matchingDataset.getDateOfBirths().get(0).isVerified()).isTrue();
    }

    @Test
    public void transformShouldTransformAnAssertionIntoAMatchingDatasetWithNonLatinNames() {
        Attribute firstname = anEidasFirstName("Bob", true);
        firstname.getAttributeValues().add(getCurrentGivenName("Βαρίδι", false));
        Attribute surname = anEidasFamilyName("Smith", true);
        surname.getAttributeValues().add(getCurrentFamilyName("Σιδηρουργός", false));
        Instant dob = BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1986-12-05");
        Attribute dateOfBirth = anEidasDateOfBirth(dob);

        // Ensure that the unmarshaller does not error when provided a gender
        Attribute gender = anEidasGender(Gender.MALE.getValue());

        PersonIdentifier personIdentifier = new PersonIdentifierBuilder().buildObject();
        personIdentifier.setPersonIdentifier("PID12345");
        Attribute personalIdentifier = PersonIdentifierAttributeBuilder.aPersonIdentifier().withValue(personIdentifier).build();
        Assertion originalAssertion = AssertionBuilder.anEidasMatchingDatasetAssertion(firstname, surname, dateOfBirth, personalIdentifier,
                Optional.of(gender)).buildUnencrypted();

        MatchingDataset matchingDataset = unmarshaller.fromAssertion(originalAssertion);

        assertThat(matchingDataset.getFirstNames().get(0).getValue()).isEqualTo("Bob");
        assertThat(matchingDataset.getFirstNames().get(0).getNonLatinScriptValue()).isEqualTo("Βαρίδι");
        assertThat(matchingDataset.getSurnames().get(0).getValue()).isEqualTo("Smith");
        assertThat(matchingDataset.getSurnames().get(0).getNonLatinScriptValue()).isEqualTo("Σιδηρουργός");
        assertThat(matchingDataset.getDateOfBirths().get(0).getValue()).isEqualTo(dob);
        assertThat(matchingDataset.getPersonalId()).isEqualTo("PID12345");
        assertThat(matchingDataset.getGender()).isNotPresent();

        assertThat(matchingDataset.getFirstNames().get(0).isVerified()).isTrue();
        assertThat(matchingDataset.getSurnames().get(0).isVerified()).isTrue();
        assertThat(matchingDataset.getDateOfBirths().get(0).isVerified()).isTrue();
    }

    private Attribute anEidasAttribute(String name, AttributeValue value) {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setName(name);
        attribute.getAttributeValues().add(value);
        return attribute;
    }

    private Attribute anEidasFirstName(String firstName, boolean isLatinScript) {
        CurrentGivenName firstNameValue = getCurrentGivenName(firstName, isLatinScript);
        return anEidasAttribute(IdaConstants.Eidas_Attributes.FirstName.NAME, firstNameValue);
    }

    private CurrentGivenName getCurrentGivenName(String firstName, boolean isLatinScript) {
        CurrentGivenName firstNameValue = new CurrentGivenNameBuilder().buildObject();
        firstNameValue.setFirstName(firstName);
        firstNameValue.setIsLatinScript(isLatinScript);
        return firstNameValue;
    }

    private Attribute anEidasFamilyName(String familyName, boolean isLatinScript) {
        CurrentFamilyName currentFamilyName = getCurrentFamilyName(familyName, isLatinScript);
        return anEidasAttribute(IdaConstants.Eidas_Attributes.FamilyName.NAME, currentFamilyName);
    }

    private CurrentFamilyName getCurrentFamilyName(String familyName, boolean isLatinScript) {
        CurrentFamilyName currentFamilyName = new CurrentFamilyNameBuilder().buildObject();
        currentFamilyName.setFamilyName(familyName);
        currentFamilyName.setIsLatinScript(isLatinScript);
        return currentFamilyName;
    }

    private Attribute anEidasDateOfBirth(Instant dob) {
        DateOfBirth dateOfBirth = new DateOfBirthBuilder().buildObject();
        dateOfBirth.setDateOfBirth(dob);
        return anEidasAttribute(IdaConstants.Eidas_Attributes.DateOfBirth.NAME, dateOfBirth);
    }

    private Attribute anEidasGender(String gender) {
        EidasGender eidasGenderValue = new EidasGenderBuilder().buildObject();
        eidasGenderValue.setValue(gender);
        return anEidasAttribute(IdaConstants.Eidas_Attributes.Gender.NAME, eidasGenderValue);
    }
}
