package stubidp.saml.utils.hub.factories;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import stubidp.saml.extensions.IdaConstants.Eidas_Attributes;
import stubidp.saml.extensions.IdaConstants.Eidas_Attributes.FamilyName;
import stubidp.saml.extensions.IdaConstants.Eidas_Attributes.FirstName;
import stubidp.saml.extensions.extensions.eidas.CurrentAddress;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.eidas.EidasGender;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentAddressBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentFamilyNameBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentGivenNameBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.DateOfBirthBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.EidasGenderBuilder;
import stubidp.saml.extensions.extensions.eidas.impl.PersonIdentifierBuilder;

import java.time.Instant;
import java.util.Arrays;

public class EidasAttributeFactory {

    public EidasAttributeFactory() {
    }

    private Attribute buildAttribute(String friendlyName, String name, AttributeValue... attributeValues) {
        Attribute attribute = new AttributeBuilder().buildObject();
        attribute.setFriendlyName(friendlyName);
        attribute.setName(name);
        attribute.setNameFormat(Attribute.URI_REFERENCE);
        attribute.getAttributeValues().addAll(Arrays.asList(attributeValues));
        return attribute;
    }

    public Attribute createFirstNameAttribute(String firstName) {
        CurrentGivenName firstNameAttributeValue = new CurrentGivenNameBuilder().buildObject();
        firstNameAttributeValue.setFirstName(firstName);
        return buildAttribute(FirstName.FRIENDLY_NAME, FirstName.NAME, firstNameAttributeValue);
    }

    public Attribute createFamilyName(String familyName) {
        CurrentFamilyName familyNameAttributeValue = new CurrentFamilyNameBuilder().buildObject();
        familyNameAttributeValue.setFamilyName(familyName);
        return buildAttribute(FamilyName.FRIENDLY_NAME, FamilyName.NAME, familyNameAttributeValue);
    }

    public Attribute createDateOfBirth(Instant dateOfBirth) {
        DateOfBirth dateOfBirthAttributeValue = new DateOfBirthBuilder().buildObject();
        dateOfBirthAttributeValue.setDateOfBirth(dateOfBirth);
        return buildAttribute(Eidas_Attributes.DateOfBirth.FRIENDLY_NAME, Eidas_Attributes.DateOfBirth.NAME, dateOfBirthAttributeValue);
    }

    public Attribute createPersonIdentifier(String personIdentifier) {
        PersonIdentifier personIdentifierAttributeValue = new PersonIdentifierBuilder().buildObject();
        personIdentifierAttributeValue.setPersonIdentifier(personIdentifier);
        return buildAttribute(Eidas_Attributes.PersonIdentifier.FRIENDLY_NAME, Eidas_Attributes.PersonIdentifier.NAME, personIdentifierAttributeValue);
    }

    public Attribute createCurrentAddress(String address) {
        CurrentAddress currentAddressAttributeValue = new CurrentAddressBuilder().buildObject();
        currentAddressAttributeValue.setCurrentAddress(address);
        return buildAttribute(Eidas_Attributes.CurrentAddress.FRIENDLY_NAME, Eidas_Attributes.CurrentAddress.NAME, currentAddressAttributeValue);
    }

    public Attribute createGender(String gender) {
        EidasGender eidasGenderAttributeValue = new EidasGenderBuilder().buildObject();
        eidasGenderAttributeValue.setValue(gender);
        return buildAttribute(Eidas_Attributes.Gender.FRIENDLY_NAME, Eidas_Attributes.Gender.NAME, eidasGenderAttributeValue);
    }
}
