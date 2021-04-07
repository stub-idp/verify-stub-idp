package stubidp.saml.extensions.extensions.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.PersonName;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PersonNameUnmarshallerTest extends OpenSAMLRunner {
    @Test
    void unmarshall_shouldSetValue() throws Exception {
        PersonName personName = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:PersonNameType"
                       ida:Language="en-GB">
                   John</saml:AttributeValue>"""
        );

        assertThat(personName.getValue()).isEqualTo("John");
    }

    @Test
    void unmarshall_shouldSetLanguage() throws Exception {
        PersonName personName = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:PersonNameType"
                       ida:Language="en-GB">
                   John</saml:AttributeValue>"""
        );

        assertThat(personName.getLanguage()).isEqualTo("en-GB");
    }

    @Test
    void unmarshall_shouldSetVerifiedWhenTrue() throws Exception {
        PersonName personName = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:PersonNameType"
                       ida:Verified="true">
                   John</saml:AttributeValue>"""
        );

        assertThat(personName.getVerified()).isEqualTo(true);
    }

    @Test
    void unmarshall_shouldSetVerifiedWhenFalse() throws Exception {
        PersonName personName = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:PersonNameType"
                       ida:Verified="true">
                   John</saml:AttributeValue>"""
        );

        assertThat(personName.getVerified()).isEqualTo(true);
    }

    @Test
    void unmarshall_shouldSetVerifiedToDefaultWhenAbsent() throws Exception {
        PersonName personName = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:PersonNameType">
                   John</saml:AttributeValue>"""
        );

        assertThat(personName.getVerified()).isEqualTo(false);
    }
}
