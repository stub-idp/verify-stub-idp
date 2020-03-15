package stubidp.saml.utils.hub.factories;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.utils.core.domain.Address;
import stubidp.saml.utils.core.domain.Gender;
import stubidp.saml.utils.core.domain.SimpleMdsValue;

import java.time.Instant;
import java.util.List;

public interface AttributeFactory {
    Attribute createFirstnameAttribute(List<SimpleMdsValue<String>> firstnames);

    Attribute createMiddlenamesAttribute(List<SimpleMdsValue<String>> middlenames);

    Attribute createSurnameAttribute(List<SimpleMdsValue<String>> surname);

    Attribute createGenderAttribute(SimpleMdsValue<Gender> gender);

    Attribute createDateOfBirthAttribute(List<SimpleMdsValue<Instant>> dateOfBirths);

    Attribute createCurrentAddressesAttribute(List<Address> currentAddresses);

    Attribute createPreviousAddressesAttribute(List<Address> previousAddresses);

    Attribute createCycle3DataAttribute(String attributeName, String cycle3Data);

    Attribute createIdpFraudEventIdAttribute(String eventId);

    Attribute createGpg45StatusAttribute(String indicator);

    Attribute createUserIpAddressAttribute(String userIpAddress);
}
