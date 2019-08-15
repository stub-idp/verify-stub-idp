package stubidp.saml.extensions.extensions.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.Date;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUnmarshallerTest extends OpenSAMLRunner {
    @Test
    public void unmarshall_shouldSetValue() throws Exception {
        Date dateTime = Utils.unmarshall("" +
                "<saml:AttributeValue " +
                "       xmlns:ida=\"http://www.cabinetoffice.gov.uk/resource-library/ida/attributes\"\n " +
                "       xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n" +
                "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "       xsi:type=\"ida:DateType\">\n" +
                "   1994-11-05" +
                "</saml:AttributeValue>"
        );

        assertThat(dateTime.getValue()).isEqualTo("1994-11-05");
    }

    @Test
    public void unmarshall_shouldSetVerifiedWhenTrue() throws Exception {
        Date dateTime = Utils.unmarshall("" +
                "<saml:AttributeValue " +
                "       xmlns:ida=\"http://www.cabinetoffice.gov.uk/resource-library/ida/attributes\"\n " +
                "       xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n" +
                "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "       xsi:type=\"ida:DateType\"\n" +
                "       ida:Verified=\"true\">\n" +
                "   1994-11-05" +
                "</saml:AttributeValue>"
        );

        assertThat(dateTime.getVerified()).isEqualTo(true);
    }

    @Test
    public void unmarshall_shouldSetVerifiedWhenFalse() throws Exception {
        Date dateTime = Utils.unmarshall("" +
                "<saml:AttributeValue " +
                "       xmlns:ida=\"http://www.cabinetoffice.gov.uk/resource-library/ida/attributes\"\n " +
                "       xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n" +
                "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "       xsi:type=\"ida:DateType\"\n" +
                "       ida:Verified=\"false\">\n" +
                "   1994-11-05" +
                "</saml:AttributeValue>"
        );

        assertThat(dateTime.getVerified()).isEqualTo(false);
    }

    @Test
    public void unmarshall_shouldSetVerifiedWhenToDefaultValueWhenAbsent() throws Exception {
        Date dateTime = Utils.unmarshall("" +
                "<saml:AttributeValue " +
                "       xmlns:ida=\"http://www.cabinetoffice.gov.uk/resource-library/ida/attributes\"\n " +
                "       xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n" +
                "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "       xsi:type=\"ida:DateType\">\n" +
                "   1994-11-05" +
                "</saml:AttributeValue>"
        );

        assertThat(dateTime.getVerified()).isEqualTo(false);
    }
}
