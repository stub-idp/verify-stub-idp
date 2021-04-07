package stubidp.saml.extensions.extensions.eidas.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PersonIdentifierUnmarshallerTest extends OpenSAMLRunner {
    @Test
    void shouldUnmarshallPersonIdentifier() throws Exception {
        final PersonIdentifier personIdentifier = Utils.unmarshall("""
                <saml2:AttributeValue xmlns:eidas-natural="http://eidas.europa.eu/attributes/naturalperson"
                    xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="eidas-natural:PersonIdentifierType">
                UK/GB/12345</saml2:AttributeValue>"""
        );

        assertThat(personIdentifier.getPersonIdentifier()).isEqualTo("UK/GB/12345");
    }
}
