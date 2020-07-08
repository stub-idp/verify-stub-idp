package stubidp.saml.utils.hub.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.extensions.IdaConstants.Eidas_Attributes;
import stubidp.saml.extensions.IdaConstants.Eidas_Attributes.FamilyName;
import stubidp.saml.extensions.IdaConstants.Eidas_Attributes.FirstName;
import stubidp.saml.extensions.extensions.eidas.CurrentAddress;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.eidas.EidasGender;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.test.OpenSAMLRunner;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opensaml.saml.saml2.core.Attribute.URI_REFERENCE;

public class EidasAttributeFactoryTest extends OpenSAMLRunner {

    private EidasAttributeFactory eidasAttributeFactory;

    @BeforeEach
    public void setup() {
        eidasAttributeFactory = new EidasAttributeFactory();
    }

    @Test
    public void createFirstNameAttribute() {
        final String firstName = "Bob";
        final Attribute attribute = eidasAttributeFactory.createFirstNameAttribute(firstName);

        assertThat(attribute.getFriendlyName()).isEqualTo(FirstName.FRIENDLY_NAME);
        assertThat(attribute.getName()).isEqualTo(FirstName.NAME);
        assertThat(attribute.getNameFormat()).isEqualTo(URI_REFERENCE);
        assertThat(attribute.getAttributeValues().size()).isEqualTo(1);
        CurrentGivenName currentGivenName = (CurrentGivenName) attribute.getAttributeValues().get(0);
        assertThat(currentGivenName.getFirstName()).isEqualTo(firstName);
    }

    @Test
    public void createFamilyName() {
        final String familyName = "Smith";
        final Attribute attribute = eidasAttributeFactory.createFamilyName(familyName);

        assertThat(attribute.getFriendlyName()).isEqualTo(FamilyName.FRIENDLY_NAME);
        assertThat(attribute.getName()).isEqualTo(FamilyName.NAME);
        assertThat(attribute.getNameFormat()).isEqualTo(URI_REFERENCE);
        assertThat(attribute.getAttributeValues().size()).isEqualTo(1);
        CurrentFamilyName currentFamilyName = (CurrentFamilyName) attribute.getAttributeValues().get(0);
        assertThat(currentFamilyName.getFamilyName()).isEqualTo(familyName);
    }

    @Test
    public void createDateOfBirth() {
        final Instant dateOfBirth = BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1965-01-01");

        final Attribute attribute = eidasAttributeFactory.createDateOfBirth(dateOfBirth);

        assertThat(attribute.getFriendlyName()).isEqualTo(Eidas_Attributes.DateOfBirth.FRIENDLY_NAME);
        assertThat(attribute.getName()).isEqualTo((Eidas_Attributes.DateOfBirth.NAME));
        assertThat(attribute.getNameFormat()).isEqualTo(URI_REFERENCE);
        assertThat(attribute.getAttributeValues().size()).isEqualTo(1);
        DateOfBirth date = (DateOfBirth) attribute.getAttributeValues().get(0);
        assertThat(date.getDateOfBirth()).isEqualTo(dateOfBirth);
    }

    @Test
    public void createPersonIdentifier() {
        final String personIdentifier = "UK/GB/12346";
        final Attribute attribute = eidasAttributeFactory.createPersonIdentifier(personIdentifier);

        assertThat(attribute.getFriendlyName()).isEqualTo(Eidas_Attributes.PersonIdentifier.FRIENDLY_NAME);
        assertThat(attribute.getName()).isEqualTo(Eidas_Attributes.PersonIdentifier.NAME);
        assertThat(attribute.getNameFormat()).isEqualTo(URI_REFERENCE);
        assertThat(attribute.getAttributeValues().size()).isEqualTo(1);
        PersonIdentifier pid = (PersonIdentifier) attribute.getAttributeValues().get(0);
        assertThat(pid.getPersonIdentifier()).isEqualTo(personIdentifier);
    }

    @Test
    public void createCurrentAddress() {
        final String currentAddress = "PGVpZGFzLW5hdHVyYWw6RnVsbEN2YWRkcmVzcz5DdXJyZW50IEFkZHJlc3M8L2VpZGFzLW5hdHVyYWw6RnVsbEN2YWRkcmVzcz4K";
        final Attribute attribute = eidasAttributeFactory.createCurrentAddress(currentAddress);

        assertThat(attribute.getFriendlyName()).isEqualTo(Eidas_Attributes.CurrentAddress.FRIENDLY_NAME);
        assertThat(attribute.getName()).isEqualTo(Eidas_Attributes.CurrentAddress.NAME);
        assertThat(attribute.getNameFormat()).isEqualTo(URI_REFERENCE);
        assertThat(attribute.getAttributeValues().size()).isEqualTo(1);
        CurrentAddress address = (CurrentAddress) attribute.getAttributeValues().get(0);
        assertThat(address.getCurrentAddress()).isEqualTo(currentAddress);
    }

    @Test
    public void createGender() {
        final String gender = "Male";
        final Attribute attribute = eidasAttributeFactory.createGender(gender);

        assertThat(attribute.getFriendlyName()).isEqualTo(Eidas_Attributes.Gender.FRIENDLY_NAME);
        assertThat(attribute.getName()).isEqualTo(Eidas_Attributes.Gender.NAME);
        assertThat(attribute.getNameFormat()).isEqualTo(URI_REFERENCE);
        assertThat(attribute.getAttributeValues().size()).isEqualTo(1);
        EidasGender value = (EidasGender) attribute.getAttributeValues().get(0);
        assertThat(value.getValue()).isEqualTo(gender);
    }
}
