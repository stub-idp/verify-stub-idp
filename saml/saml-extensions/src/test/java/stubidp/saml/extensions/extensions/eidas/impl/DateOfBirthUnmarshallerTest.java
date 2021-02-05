package stubidp.saml.extensions.extensions.eidas.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DateOfBirthUnmarshallerTest extends OpenSAMLRunner {
    @Test
    void shouldUnmarshallDateOfBirth() throws Exception {
        final DateOfBirth dateOfBirth = Utils.unmarshall("""
                <saml2:AttributeValue xmlns:eidas-natural="http://eidas.europa.eu/attributes/naturalperson"
                    xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="eidas-natural:DateOfBirthType">
                1965-01-01</saml2:AttributeValue>"""
        );

        assertThat(dateOfBirth.getDateOfBirth()).isEqualTo("1965-01-01");
    }
}
