package stubidp.saml.extensions.extensions.eidas.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class CurrentGivenNameUnmarshallerTest extends OpenSAMLRunner {

    @Test
    public void shouldUnmarshallCurrentGivenNameValue() throws Exception {
        final CurrentGivenName currentGivenName = Utils.unmarshall(getCurrentGivenNameSamlString(true));

        assertThat(currentGivenName.getFirstName()).isEqualTo("Javier");
    }

    @Test
    public void shouldUnmarshallLatinScriptValueWhenAbsent() throws Exception {
        final CurrentGivenName currentGivenName = Utils.unmarshall(getCurrentGivenNameSamlString(true));

        assertThat(currentGivenName.isLatinScript()).isEqualTo(true);
    }

    @Test
    public void shouldUnmarshallLatinScriptValueWhenPresent() throws Exception {
        final CurrentGivenName currentGivenName = Utils.unmarshall(getCurrentGivenNameSamlString(false));

        assertThat(currentGivenName.isLatinScript()).isEqualTo(false);
    }

    private String getCurrentGivenNameSamlString(boolean isLatinScript) {
        return String.format(
                "<saml2:AttributeValue " +
                "%s" +
                "   xmlns:eidas-natural=\"http://eidas.europa.eu/attributes/naturalperson\"\n " +
                "   xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n " +
                "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n " +
                "   xsi:type=\"eidas-natural:CurrentGivenNameType\">\n" +
                "Javier" +
                "</saml2:AttributeValue>", isLatinScript ? "" : "LatinScript=\"false\"");
    }
}
