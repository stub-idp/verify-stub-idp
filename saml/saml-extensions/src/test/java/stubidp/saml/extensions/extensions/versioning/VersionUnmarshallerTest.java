package stubidp.saml.extensions.extensions.versioning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.saml.extensions.extensions.versioning.Version;
import stubidp.saml.Utils;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionUnmarshallerTest extends OpenSAMLRunner {

    @Test
    public void shouldUnMarshallVersion() throws Exception {
        Version versionAttributeValue = Utils.unmarshall("" +
            "<saml2:AttributeValue " +
            "           xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\" " +
            "           xmlns:metric=\"http://www.cabinetoffice.gov.uk/resource-library/ida/metrics\" " +
            "           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"metric:VersionType\">\n" +
            "   <metric:ApplicationVersion>" + "some-version-value" + "</metric:ApplicationVersion>\n" +
            "</saml2:AttributeValue>"
        );

        assertThat(versionAttributeValue.getApplicationVersion().getValue()).isEqualTo("some-version-value");
    }
}