package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.CurrentAddress;
import stubidp.saml.extensions.extensions.eidas.impl.CurrentAddressBuilder;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURUAL_PREFIX;

public class CurrentAddressMarshallerTest extends OpenSAMLRunner {

    @Test
    public void shouldMarshallCurrentAddress() throws Exception {
        final String address = "PGVpZGFzLW5hdHVyYWw6RnVsbEN2YWRkcmVzcz5DdXJyZW50IEFkZHJlc3M8L2VpZGFzLW5hdHVyYWw6RnVsbEN2YWRkcmVzcz4K";
        final CurrentAddress currentAddress = new CurrentAddressBuilder().buildObject();
        final Marshaller currentAddressMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(currentAddress);
        currentAddress.setCurrentAddress(address);

        final Element marshalledElement = currentAddressMarshaller.marshall(currentAddress);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(CurrentAddress.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", EIDAS_NATURUAL_PREFIX, CurrentAddress.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(address);
    }
}
