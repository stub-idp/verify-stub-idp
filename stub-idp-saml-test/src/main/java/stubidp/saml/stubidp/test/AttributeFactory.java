package stubidp.saml.stubidp.test;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import stubidp.saml.extensions.IdaConstants.Eidas_Attributes;
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
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;

import java.util.Arrays;

public class AttributeFactory {

    private static Attribute buildAttribute(String friendlyName, String name, AttributeValue... attributeValues) {
        Attribute attribute = new AttributeBuilder().buildObject();
        attribute.setFriendlyName(friendlyName);
        attribute.setName(name);
        attribute.setNameFormat(Attribute.URI_REFERENCE);
        attribute.getAttributeValues().addAll(Arrays.asList(attributeValues));
        return attribute;
    }

    public static Attribute genderAttribute(String gender) {
        EidasGender eidasGenderAttributeValue = new EidasGenderBuilder().buildObject();
        eidasGenderAttributeValue.setValue(gender);
        return buildAttribute(Eidas_Attributes.Gender.FRIENDLY_NAME, Eidas_Attributes.Gender.NAME, eidasGenderAttributeValue);
    }

    public static Attribute currentAddressAttribute(String address) {
        CurrentAddress currentAddressAttributeValue = new CurrentAddressBuilder().buildObject();
        currentAddressAttributeValue.setCurrentAddress("PGVpZGFzLW5hdHVyYWw6RnVsbEN2YWRkcmVzcz5DdXJyZW50IEFkZHJlc3M8L2VpZGFzLW5hdHVyYWw6RnVsbEN2YWRkcmVzcz4K");
        return buildAttribute(Eidas_Attributes.CurrentAddress.FRIENDLY_NAME, Eidas_Attributes.CurrentAddress.NAME, currentAddressAttributeValue);
    }

    public static Attribute firstNameAttribute(String firstName) {
        CurrentGivenName firstNameAttributeValue = new CurrentGivenNameBuilder().buildObject();
        firstNameAttributeValue.setFirstName("Javier");
        return buildAttribute(Eidas_Attributes.FirstName.FRIENDLY_NAME, Eidas_Attributes.FirstName.NAME, firstNameAttributeValue);
    }

    public static Attribute familyNameAttribute(String familyName) {
        CurrentFamilyName familyNameAttributeValue = new CurrentFamilyNameBuilder().buildObject();
        familyNameAttributeValue.setFamilyName("Garcia");
        return buildAttribute(Eidas_Attributes.FamilyName.FRIENDLY_NAME, Eidas_Attributes.FamilyName.NAME, familyNameAttributeValue);
    }

    public static Attribute dateOfBirthAttribute(String dateOfBirth) {
        DateOfBirth dateOfBirthAttributeValue = new DateOfBirthBuilder().buildObject();
        dateOfBirthAttributeValue.setDateOfBirth(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1965-01-01"));
        return buildAttribute(Eidas_Attributes.DateOfBirth.FRIENDLY_NAME, Eidas_Attributes.DateOfBirth.NAME, dateOfBirthAttributeValue);
    }

    public static Attribute personIdentifierAttribute(String personIdentifier) {
        PersonIdentifier personIdentifierAttributeValue = new PersonIdentifierBuilder().buildObject();
        personIdentifierAttributeValue.setPersonIdentifier("UK/GB/12345");
        return buildAttribute(Eidas_Attributes.PersonIdentifier.FRIENDLY_NAME, Eidas_Attributes.PersonIdentifier.NAME, personIdentifierAttributeValue);
    }
}
