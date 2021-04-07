package stubidp.saml.extensions.extensions.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.Gender;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class GenderUnmarshallerTest extends OpenSAMLRunner {
    @Test
    void unmarshall_shouldSetValue() throws Exception {
        Gender gender = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:GenderType"
                       ida:Language="en-GB">
                   Female</saml:AttributeValue>"""
        );

        assertThat(gender.getValue()).isEqualTo("Female");
    }

    @Test
    void unmarshall_shouldSetVerifiedWhenTrue() throws Exception {
        Gender gender = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:GenderType"
                       ida:Verified="true">
                   John</saml:AttributeValue>"""
        );

        assertThat(gender.getVerified()).isEqualTo(true);
    }

    @Test
    void unmarshall_shouldSetVerifiedWhenFalse() throws Exception {
        Gender gender = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:GenderType"
                       ida:Verified="true">
                   John</saml:AttributeValue>"""
        );

        assertThat(gender.getVerified()).isEqualTo(true);
    }

    @Test
    void unmarshall_shouldSetVerifiedToDefaultWhenAbsent() throws Exception {
        Gender gender = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:GenderType">
                   John</saml:AttributeValue>"""
        );

        assertThat(gender.getVerified()).isEqualTo(false);
    }
}
