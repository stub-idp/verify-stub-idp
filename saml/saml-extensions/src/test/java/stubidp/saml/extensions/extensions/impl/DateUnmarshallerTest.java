package stubidp.saml.extensions.extensions.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.Date;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DateUnmarshallerTest extends OpenSAMLRunner {
    @Test
    void unmarshall_shouldSetValue() throws Exception {
        Date dateTime = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:DateType">
                   1994-11-05</saml:AttributeValue>"""
        );

        assertThat(dateTime.getValue()).isEqualTo("1994-11-05");
    }

    @Test
    void unmarshall_shouldSetVerifiedWhenTrue() throws Exception {
        Date dateTime = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:DateType"
                       ida:Verified="true">
                   1994-11-05</saml:AttributeValue>"""
        );

        assertThat(dateTime.getVerified()).isEqualTo(true);
    }

    @Test
    void unmarshall_shouldSetVerifiedWhenFalse() throws Exception {
        Date dateTime = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:DateType"
                       ida:Verified="false">
                   1994-11-05</saml:AttributeValue>"""
        );

        assertThat(dateTime.getVerified()).isEqualTo(false);
    }

    @Test
    void unmarshall_shouldSetVerifiedWhenToDefaultValueWhenAbsent() throws Exception {
        Date dateTime = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:DateType">
                   1994-11-05</saml:AttributeValue>"""
        );

        assertThat(dateTime.getVerified()).isEqualTo(false);
    }
}
