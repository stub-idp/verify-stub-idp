package stubidp.saml.extensions.extensions.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.Verified;
import stubidp.saml.extensions.extensions.impl.VerifiedBuilder;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.IDA_PREFIX;

class VerifiedMarshallerTest extends OpenSAMLRunner {

    private Marshaller marshaller;
    private Verified verifiedAttributeValue;

    @BeforeEach
    void setUp() {
        verifiedAttributeValue = new VerifiedBuilder().buildObject();
        marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(verifiedAttributeValue);
    }

    @Test
    void marshall_shouldMarshallVerifiedValueWhenTrue() throws Exception {
        verifiedAttributeValue.setValue(true);

        Element marshalledElement = marshaller.marshall(verifiedAttributeValue);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(Verified.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", IDA_PREFIX, Verified.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo("true");
    }

    @Test
    void marshall_shouldMarshallVerifiedValueWhenFalse() throws Exception {
        verifiedAttributeValue.setValue(false);

        Element marshalledElement = marshaller.marshall(verifiedAttributeValue);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(Verified.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", IDA_PREFIX, Verified.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo("false");
    }

    @Test
    void marshall_shouldEnsureXsiNamespaceDefinitionIsIncluded() throws Exception {
        Element marshalledElement = marshaller.marshall(new VerifiedBuilder().buildObject());
        assertThat(marshalledElement.hasAttributeNS(XMLConstants.XMLNS_NS, XMLConstants.XSI_PREFIX)).isTrue();
    }
}
