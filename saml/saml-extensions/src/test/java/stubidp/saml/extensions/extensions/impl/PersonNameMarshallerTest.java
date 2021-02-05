package stubidp.saml.extensions.extensions.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.PersonName;
import stubidp.saml.test.OpenSAMLRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.IDA_NS;
import static stubidp.saml.extensions.IdaConstants.IDA_PREFIX;

class PersonNameMarshallerTest extends OpenSAMLRunner {

    private Marshaller marshaller;
    private PersonName personName;

    @BeforeEach
    void setUp() {
        personName = new PersonNameBuilder().buildObject();
        marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(personName);
    }

    @Test
    void marshall_shouldMarshallPersonName() throws Exception {
        String name = "John";
        String language = "en-GB";
        personName.setValue(name);
        personName.setLanguage(language);

        Element marshalledElement = marshaller.marshall(personName);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(PersonName.DEFAULT_ELEMENT_NAME.getNamespaceURI());

        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", IDA_PREFIX, PersonName.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getAttributeNS(IDA_NS, PersonName.LANGUAGE_ATTRIB_NAME)).isEqualTo(language);
        assertThat(marshalledElement.getTextContent()).isEqualTo(name);
    }

    @Test
    void marshall_shouldEnsureXsiNamespaceDefinitionIsInluded() throws Exception {
        Element marshalledElement = marshaller.marshall(new PersonNameBuilder().buildObject());

        assertThat(marshalledElement.hasAttributeNS(XMLConstants.XMLNS_NS, XMLConstants.XSI_PREFIX)).isTrue();
    }

    @Test
    void marshall_shouldMarshallFromDateInCorrectFormat() throws Exception {
        String fromDate = "2012-02-09";
        personName.setFrom(LocalDate.parse(fromDate));

        Element marshalledElement = marshaller.marshall(personName);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, PersonName.FROM_ATTRIB_NAME).getValue()).isEqualTo(fromDate);
    }

    @Test
    void marshall_shouldMarshallFromDateWithNamespacePrefix() throws Exception {
        personName.setFrom(LocalDate.parse("2012-02-09"));

        Element marshalledElement = marshaller.marshall(personName);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, PersonName.FROM_ATTRIB_NAME).getPrefix()).isEqualTo(IDA_PREFIX);
    }

    @Test
    void marshall_shouldMarshallToDateInCorrectFormat() throws Exception {
        String toDate = "2012-02-09";
        personName.setTo(LocalDate.parse(toDate));

        Element marshalledElement = marshaller.marshall(personName);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, PersonName.TO_ATTRIB_NAME).getValue()).isEqualTo(toDate);
    }

    @Test
    void marshall_shouldMarshallToDateWithNamespacePrefix() throws Exception {
        personName.setTo(LocalDate.parse("2012-02-09"));

        Element marshalledElement = marshaller.marshall(personName);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, PersonName.TO_ATTRIB_NAME).getPrefix()).isEqualTo(IDA_PREFIX);
    }

    @Test
    void marshall_shouldMarshallVerifiedWhenTrue() throws Exception {
        checkMarshallingVerifiedAttributeWithValue(true);
    }

    @Test
    void marshall_shouldMarshallVerifiedWhenFalse() throws Exception {
        checkMarshallingVerifiedAttributeWithValue(false);
    }

    @Test
    void marshall_shouldMarshallVerifiedWithNamespacePrefix() throws Exception {
        personName.setVerified(true);

        Element marshalledElement = marshaller.marshall(personName);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, PersonName.VERIFIED_ATTRIB_NAME).getPrefix()).isEqualTo(IDA_PREFIX);
    }

    private void checkMarshallingVerifiedAttributeWithValue(boolean verifiedValue) throws MarshallingException {
        personName.setVerified(verifiedValue);

        Element marshalledElement = marshaller.marshall(personName);

        assertThat(Boolean.parseBoolean(marshalledElement.getAttributeNodeNS(IDA_NS, PersonName.VERIFIED_ATTRIB_NAME).getValue())).isEqualTo(verifiedValue);
    }
}
