package stubidp.test.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import stubidp.test.integration.support.IntegrationTestHelper;
import stubidp.test.integration.support.StubIdpAppExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.stubidp.builders.StubIdpBuilder.aStubIdp;

@ExtendWith(DropwizardExtensionsSupport.class)
public class EidasCountryMetadataIntegrationTests extends IntegrationTestHelper {

    private static final String COUNTRY_NAME = "country1";
    public static final String DISPLAY_NAME = "User Repository Identity Service";
    private final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    public static final StubIdpAppExtension applicationRule = new StubIdpAppExtension()
            .withStubIdp(aStubIdp()
                    .withId(COUNTRY_NAME)
                    .withDisplayName(DISPLAY_NAME)
                    .build());

    @BeforeEach
    public void refreshMetadata() {
        client.target("http://localhost:"+applicationRule.getAdminPort()+"/tasks/connector-metadata-refresh").request().post(Entity.text(""));
    }

    @Test
    public void countryMetadataShouldContainCorrectEntityIdAndSsoUrl() {
        String baseUrl = applicationRule.getConfiguration().getEuropeanIdentityConfiguration().getStubCountryBaseUrl();
        String metadataEndpoint = baseUrl + "/stub-country/ServiceMetadata";
        String expectedSsoUrl = baseUrl + "/eidas/stub-country/SAML2/SSO";

        Response response = client.target("http://localhost:"+applicationRule.getPort(0) + "/stub-country/ServiceMetadata").request().get();
        Document metadata = response.readEntity(Document.class);

        String entityId =  metadata.getDocumentElement().getAttributes().getNamedItem("entityID").getNodeValue();
        String ssoUrl =  getChildByTagName(getChildByTagName(metadata.getDocumentElement(), "md:IDPSSODescriptor"), "md:SingleSignOnService").getAttributes().getNamedItem("Location").getNodeValue();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(entityId).isEqualTo(metadataEndpoint);
        assertThat(ssoUrl).isEqualTo(expectedSsoUrl);
    }

    private Node getChildByTagName(Node element, String tagName) {
        NodeList children = element.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals(tagName)) {
                return children.item(i);
            }
        }
        return null;
    }
}
