package stubidp.saml.extensions.extensions.eidas.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentFamilyNameUnmarshallerTest extends OpenSAMLRunner {
    @Test
    void shouldUnmarshallCurrentFamilyNameValue() throws Exception {
        final CurrentFamilyName currentFamilyName = Utils.unmarshall(getCurrentFamilyNameSamlString(true));

        assertThat(currentFamilyName.getFamilyName()).isEqualTo("Garcia");
    }

    @Test
    void shouldUnmarshallLatinScriptValueWhenAbsent() throws Exception {
        final CurrentFamilyName currentFamilyName = Utils.unmarshall(getCurrentFamilyNameSamlString(true));

        assertThat(currentFamilyName.isLatinScript()).isEqualTo(true);
    }

    @Test
    void shouldUnmarshallLatinScriptValueWhenPresent() throws Exception {
        final CurrentFamilyName currentFamilyName = Utils.unmarshall(getCurrentFamilyNameSamlString(false));

        assertThat(currentFamilyName.isLatinScript()).isEqualTo(false);
    }

    private String getCurrentFamilyNameSamlString(boolean isLatinScript) {
        return String.format(
                "<saml2:AttributeValue " +
                "%s" +
                "   xmlns:eidas-natural=\"http://eidas.europa.eu/attributes/naturalperson\"\n " +
                "   xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n " +
                "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n " +
                "   xsi:type=\"eidas-natural:CurrentFamilyNameType\">\n" +
                "Garcia" +
                "</saml2:AttributeValue>", isLatinScript ? "" : "LatinScript=\"false\"");
    }
}
