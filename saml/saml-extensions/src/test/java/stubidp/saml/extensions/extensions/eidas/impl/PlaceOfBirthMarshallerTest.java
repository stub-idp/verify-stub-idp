package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.PlaceOfBirth;
import stubidp.saml.extensions.extensions.eidas.impl.PlaceOfBirthBuilder;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURUAL_PREFIX;

public class PlaceOfBirthMarshallerTest extends OpenSAMLRunner {

    @Test
    public void shouldMarshallPlaceOfBirth() throws Exception {
        final String place = "Peterborough";
        final PlaceOfBirth placeOfBirth = new PlaceOfBirthBuilder().buildObject();
        final Marshaller placeOfBirthMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(placeOfBirth);
        placeOfBirth.setPlaceOfBirth(place);

        final Element marshalledElement = placeOfBirthMarshaller.marshall(placeOfBirth);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(PlaceOfBirth.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", EIDAS_NATURUAL_PREFIX, PlaceOfBirth.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(place);
    }
}
