package stubidp.saml.extensions.extensions.versioning;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.versioning.Version;
import stubidp.saml.extensions.extensions.versioning.VersionBuilder;
import stubidp.saml.extensions.extensions.versioning.VersionMarshaller;
import stubidp.saml.extensions.extensions.versioning.application.ApplicationVersionImpl;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionMarshallerTest extends OpenSAMLRunner {

    private VersionMarshaller versionMarshaller = new VersionMarshaller();

    @Test
    public void shouldMarshallVersion() throws Exception {
        Version version = new VersionBuilder().buildObject();
        ApplicationVersionImpl applicationVersion = new ApplicationVersionImpl();
        applicationVersion.setValue("some-version-value");
        version.setApplicationVersion(applicationVersion);

        Element marshaledVersion = versionMarshaller.marshall(version);

        assertThat(marshaledVersion.getAttribute(XMLConstants.XMLNS_PREFIX + ":" + Version.NAMESPACE_PREFIX)).isEqualTo("http://www.cabinetoffice.gov.uk/resource-library/ida/metrics");
        assertThat(marshaledVersion.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo("metric:VersionType");
        assertThat(marshaledVersion.getFirstChild().getNodeName()).isEqualTo("metric:ApplicationVersion");
        assertThat(marshaledVersion.getFirstChild().getTextContent()).isEqualTo("some-version-value");
    }
}