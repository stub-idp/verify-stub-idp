package stubsp.stubsp.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.saml.extensions.IdaSamlBootstrap;
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
class StubSpApplicationIntegrationTest {

    static {
        IdaSamlBootstrap.bootstrap();
    }

    private static final StubSpAppExtension stubSpAppExtension = new StubSpAppExtension();

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
        assertThat(response.readEntity(String.class)).contains("hello");
    }

    @Test
    void testSecureResourceNeedsLogin() {
        Response response = get(Urls.SECURE_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("Saml Processing...");
        response = get("/assets/scripts/saml-redirect-auto-submit.js");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("submit()");
    }

    @Test
    void testAvailableServices() {
        final Response response = get(Urls.AVAILABLE_SERVICE_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        // WARNING: don't remove the type in GenericType, leads to compiler error
        final List<AvailableServiceDto> availableServices = response.readEntity(new GenericType<List<AvailableServiceDto>>() {});
        assertThat(availableServices).hasSize(1);
    }

    private Response get(String resource) {
        return client.target(UriBuilder.fromUri("http://localhost:" + stubSpAppExtension.getLocalPort() + resource).build())
                .request()
                .get();
    }
}
