package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;
import stubidp.saml.extensions.extensions.eidas.TransliterableString;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentFamilyNameBuilder;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURUAL_PREFIX;

public class CurrentFamilyNameMarshallerTest extends OpenSAMLRunner {

    private CurrentFamilyName currentFamilyName;
    private Marshaller currentFamilyNameMarshaller;

    @BeforeEach
    public void setUp() {
        currentFamilyName = new CurrentFamilyNameBuilder().buildObject();
        currentFamilyNameMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(currentFamilyName);
    }

    @Test
    public void shouldMarshallCurrentFamilyName() throws Exception {
        final String familyName = "Garcia";
        currentFamilyName.setFamilyName(familyName);

        final Element marshalledElement = currentFamilyNameMarshaller.marshall(currentFamilyName);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(CurrentFamilyName.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", EIDAS_NATURUAL_PREFIX, CurrentFamilyName.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(familyName);
    }

    @Test
    public void shouldMarshallWhenIsLatinScriptIsTrue() throws Exception {
        currentFamilyName.setIsLatinScript(true);

        final Element marshalledElement = currentFamilyNameMarshaller.marshall(currentFamilyName);

        assertThat(marshalledElement.getAttribute(TransliterableString.IS_LATIN_SCRIPT_ATTRIBUTE_NAME)).isEqualTo("");
    }

    @Test
    public void shouldMarshallWhenIsLatinScriptIsFalse() throws Exception {
        currentFamilyName.setIsLatinScript(false);

        final Element marshalledElement = currentFamilyNameMarshaller.marshall(currentFamilyName);

        assertThat(marshalledElement.getAttribute(TransliterableString.IS_LATIN_SCRIPT_ATTRIBUTE_NAME)).isEqualTo("false");
    }
}
