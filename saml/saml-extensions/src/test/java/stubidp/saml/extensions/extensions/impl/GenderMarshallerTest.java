package stubidp.saml.extensions.extensions.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Element;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.Gender;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.IDA_NS;
import static stubidp.saml.extensions.IdaConstants.IDA_PREFIX;

public class GenderMarshallerTest extends OpenSAMLRunner {

    private Marshaller marshaller;
    private Gender gender;

    @BeforeEach
    public void setUp() {
        gender = new GenderBuilder().buildObject();
        marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(gender);
    }

    @Test
    public void marshall_shouldMarshallPersonName() throws Exception {
        String name = "John";
        gender.setValue(name);

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(Gender.DEFAULT_ELEMENT_NAME.getNamespaceURI());

        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", IDA_PREFIX, Gender.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(name);
    }

    @Test
    public void marshall_shouldEnsureXsiNamespaceDefinitionIsInluded() throws Exception {
        Element marshalledElement = marshaller.marshall(new GenderBuilder().buildObject());

        assertThat(marshalledElement.hasAttributeNS(XMLConstants.XMLNS_NS, XMLConstants.XSI_PREFIX)).isTrue();
    }

    @Test
    public void marshall_shouldMarshallFromDateInCorrectFormat() throws Exception {
        String fromDate = "2012-02-09";
        gender.setFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of(fromDate));

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.FROM_ATTRIB_NAME).getValue()).isEqualTo(fromDate);
    }

    @Test
    public void marshall_shouldMarshallFromDateWithNamespacePrefix() throws Exception {
        gender.setFrom(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2012-02-09"));

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.FROM_ATTRIB_NAME).getPrefix()).isEqualTo(IdaConstants.IDA_PREFIX);
    }

    @Test
    public void marshall_shouldMarshallToDateInCorrectFormat() throws Exception {
        String toDate = "2012-02-09";
        final Gender personName = new GenderBuilder().buildObject();
        personName.setTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of(toDate));

        Element marshalledElement = marshaller.marshall(personName);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.TO_ATTRIB_NAME).getValue()).isEqualTo(toDate);
    }

    @Test
    public void marshall_shouldMarshallToDateWithNamespacePrefix() throws Exception {
        gender.setTo(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("2012-02-09"));

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.TO_ATTRIB_NAME).getPrefix()).isEqualTo(IdaConstants.IDA_PREFIX);
    }

    @Test
    public void marshall_shouldMarshallVerifiedWhenTrue() throws Exception {
        checkMarshallingVerifiedAttributeWithValue(true);
    }

    @Test
    public void marshall_shouldMarshallVerifiedWhenFalse() throws Exception {
        checkMarshallingVerifiedAttributeWithValue(false);
    }

    @Test
    public void marshall_shouldMarshallVerifiedWithNamespacePrefix() throws Exception {
        gender.setVerified(true);

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.VERIFIED_ATTRIB_NAME).getPrefix()).isEqualTo(IdaConstants.IDA_PREFIX);
    }

    private void checkMarshallingVerifiedAttributeWithValue(boolean verifiedValue) throws MarshallingException {
        gender.setVerified(verifiedValue);

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(Boolean.parseBoolean(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.VERIFIED_ATTRIB_NAME).getValue())).isEqualTo(verifiedValue);
    }
}
