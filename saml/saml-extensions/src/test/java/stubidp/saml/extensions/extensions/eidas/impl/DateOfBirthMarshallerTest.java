package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.test.OpenSAMLRunner;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURUAL_PREFIX;

class DateOfBirthMarshallerTest extends OpenSAMLRunner {

    @Test
    void shouldMarshallDateOfBirth() throws Exception {
        final String dateString = "1965-01-01";
        final Instant date = BaseMdsSamlObjectUnmarshaller.InstantFromDate.of(dateString);
        final DateOfBirth dateOfBirth = new DateOfBirthBuilder().buildObject();
        final Marshaller dateOfBirthMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(dateOfBirth);
        dateOfBirth.setDateOfBirth(date);

        final Element marshalledElement = dateOfBirthMarshaller.marshall(dateOfBirth);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(DateOfBirth.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", EIDAS_NATURUAL_PREFIX, DateOfBirth.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(dateString);
    }
}
