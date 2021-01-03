package stubidp.saml.extensions.extensions.eidas.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.eidas.BirthName;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class BirthNameUnmarshallerTest extends OpenSAMLRunner {
    @Test
    void shouldUnmarshallBirthName() throws Exception {
        final BirthName birthName = Utils.unmarshall(
                "<saml2:AttributeValue " +
                "   xmlns:eidas-natural=\"http://eidas.europa.eu/attributes/naturalperson\"\n " +
                "   xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n " +
                "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n " +
                "   xsi:type=\"eidas-natural:BirthNameType\">\n" +
                "Sarah Jane Booth" +
                "</saml2:AttributeValue>"
        );

        assertThat(birthName.getBirthName()).isEqualTo("Sarah Jane Booth");
    }
}
