package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.BirthName;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURUAL_PREFIX;

public class BirthNameMarshallerTest extends OpenSAMLRunner {

    @Test
    public void shouldMarshallBirthName() throws Exception {
        final String fullName = "Sarah Jane Booth";
        final BirthName birthName = new BirthNameBuilder().buildObject();
        final Marshaller birthNameMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(birthName);
        birthName.setBirthName(fullName);

        final Element marshalledElement = birthNameMarshaller.marshall(birthName);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(BirthName.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", EIDAS_NATURUAL_PREFIX, BirthName.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(fullName);
    }
}
