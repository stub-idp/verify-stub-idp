package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.extensions.extensions.eidas.impl.PersonIdentifierBuilder;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURUAL_PREFIX;

public class PersonIdentifierMarshallerTest extends OpenSAMLRunner {

    @Test
    public void shouldMarshallPersonIdentifier() throws Exception {
        final String personId = "UK/GB/12345";
        final PersonIdentifier personIdentifier = new PersonIdentifierBuilder().buildObject();
        final Marshaller personIdentifierMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(personIdentifier);
        personIdentifier.setPersonIdentifier(personId);

        final Element marshalledElement = personIdentifierMarshaller.marshall(personIdentifier);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(PersonIdentifier.DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", EIDAS_NATURUAL_PREFIX, PersonIdentifier.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(personId);
    }
}
