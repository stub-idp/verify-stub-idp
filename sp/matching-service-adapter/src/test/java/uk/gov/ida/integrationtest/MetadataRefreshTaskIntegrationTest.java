package uk.gov.ida.integrationtest;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.utils.rest.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class MetadataRefreshTaskIntegrationTest {

    private static Client client;

    public static MatchingServiceAdapterAppExtension matchingServiceAdapterAppExtension = new MatchingServiceAdapterAppExtension(true);

    @BeforeAll
    public static void setUpClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(10)).build();
        client = new JerseyClientBuilder(matchingServiceAdapterAppExtension.getEnvironment()).using(jerseyClientConfiguration).build(MetadataRefreshTaskIntegrationTest.class.getSimpleName());
    }

    @Test
    public void verifyFederationMetadataRefreshTaskWorks() {
        final Response response = client.target(UriBuilder.fromUri("http://localhost")
                .path("/tasks/metadata-refresh")
                .port(matchingServiceAdapterAppExtension.getAdminPort())
                .build())
                .request()
                .post(Entity.text("refresh!"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void eidasConnectorMetadataRefreshTaskWorks() {
        final Response response = client.target(UriBuilder.fromUri("http://localhost")
                .path("/tasks/eidas-metadata-refresh")
                .port(matchingServiceAdapterAppExtension.getAdminPort())
                .build())
                .request()
                .post(Entity.text("refresh!"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
