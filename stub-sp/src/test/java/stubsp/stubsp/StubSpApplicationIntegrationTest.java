package stubsp.stubsp;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubsp.stubsp.Urls;
import stubsp.stubsp.domain.AvailableServiceDto;
import stubsp.stubsp.integration.support.StubSpAppExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class StubSpApplicationIntegrationTest {

    public static final StubSpAppExtension stubSpAppExtension = new StubSpAppExtension();

    private final Client client = JerseyClientBuilder.createClient()
            .property(ClientProperties.FOLLOW_REDIRECTS, false);

    @BeforeEach
    void refreshMetadata() {
        client.target("http://localhost:"+stubSpAppExtension.getAdminPort()+"/tasks/idp-metadata-refresh")
                .request()
                .post(Entity.text(""));
    }

    @Test
    void testJourney() {
        final Response response = get(Urls.ROOT_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("<a href=\"/stub/sp/secure\">Secure Area</a>");
    }

    @Test
    void testSecureResourceNeedsLogin() {
        Response response = get(Urls.SECURE_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("Saml Processing...");
        response = get("/assets/shared/scripts/saml-redirect-auto-submit.js");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("submit()");
    }

    @Test
    void testAvailableServices() {
        final Response response = get(Urls.AVAILABLE_SERVICE_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        // WARNING: don't remove the type in GenericType, leads to compiler error on 11.0.2+9/travis
        final List<AvailableServiceDto> availableServices = response.readEntity(new GenericType<List<AvailableServiceDto>>() {});
        assertThat(availableServices).hasSize(1);
    }

    @Test
    void testMetadata() {
        final Response response = get(Urls.SAML_FEDERATION_METADATA_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        final String metadata = response.readEntity(String.class);
        assertThat(metadata).contains("Stub Sp");
    }

    private Response get(String resource) {
        return client.target(UriBuilder.fromUri("http://localhost:" + stubSpAppExtension.getLocalPort() + resource).build())
                .request()
                .get();
    }
}
