package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.EidasGender;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURUAL_PREFIX;

class EidasGenderMarshallerTest extends OpenSAMLRunner {

    @Test
    void shouldMarshallGender() throws Exception {
        final String genderValue = "Male";
        final EidasGender eidasGender = new EidasGenderBuilder().buildObject();
        final Marshaller genderMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(eidasGender);
        eidasGender.setValue(genderValue);

        final Element marshalledElement = genderMarshaller.marshall(eidasGender);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(EidasGender.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", EIDAS_NATURUAL_PREFIX, EidasGender.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(genderValue);
    }
}
