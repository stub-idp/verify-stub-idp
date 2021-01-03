package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.extensions.extensions.eidas.TransliterableString;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentGivenNameBuilder;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURUAL_PREFIX;

class CurrentGivenNameMarshallerTest extends OpenSAMLRunner {

    private Marshaller currentGivenNameMarshaller;
    private CurrentGivenName currentGivenName;

    @BeforeEach
    void setUp() {
        currentGivenName = new CurrentGivenNameBuilder().buildObject();
        currentGivenNameMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(currentGivenName);
    }

    @Test
    void shouldMarshallCurrentGivenName() throws Exception {
        final String firstName = "Javier";
        currentGivenName.setFirstName(firstName);

        final Element marshalledElement = currentGivenNameMarshaller.marshall(currentGivenName);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(CurrentGivenName.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", EIDAS_NATURUAL_PREFIX, CurrentGivenName.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(firstName);
    }

    @Test
    void shouldMarshallWhenIsLatinScriptIsTrue() throws Exception {
        currentGivenName.setIsLatinScript(true);

        final Element marshalledElement = currentGivenNameMarshaller.marshall(currentGivenName);

        assertThat(marshalledElement.getAttribute(TransliterableString.IS_LATIN_SCRIPT_ATTRIBUTE_NAME)).isEqualTo("");
    }

    @Test
    void shouldMarshallWhenIsLatinScriptIsFalse() throws Exception {
        currentGivenName.setIsLatinScript(false);

        final Element marshalledElement = currentGivenNameMarshaller.marshall(currentGivenName);

        assertThat(marshalledElement.getAttribute(TransliterableString.IS_LATIN_SCRIPT_ATTRIBUTE_NAME)).isEqualTo("false");
    }
}
