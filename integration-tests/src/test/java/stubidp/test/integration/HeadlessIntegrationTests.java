package stubidp.test.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.stubidp.resources.idp.HeadlessIdpResource;
import stubidp.test.integration.steps.AuthnRequestSteps;
import stubidp.test.integration.support.IntegrationTestHelper;
import stubidp.test.integration.support.StubIdpAppExtension;
import stubsp.stubsp.saml.response.SamlResponseDecrypter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.metrics.prometheus.bundle.PrometheusBundle.PROMETHEUS_METRICS_RESOURCE;
import static stubidp.stubidp.builders.StubIdpBuilder.aStubIdp;

@ExtendWith(DropwizardExtensionsSupport.class)
public class HeadlessIntegrationTests extends IntegrationTestHelper {

    private static final String IDP_NAME = HeadlessIdpResource.IDP_NAME;
    private static final String DISPLAY_NAME = "User Login Identity Service";

    private final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);
    private final AuthnRequestSteps authnRequestSteps = new AuthnRequestSteps(
            client,
            IDP_NAME,
            applicationRule.getLocalPort());
    private final SamlResponseDecrypter samlResponseDecrypter = new SamlResponseDecrypter(client,
            applicationRule.getVerifyMetadataPath(),
            applicationRule.getConfiguration().getHubEntityId(),
            empty(),
            applicationRule.getAssertionConsumerServices(),
            applicationRule.getHubKeyStore(),
            applicationRule.getEidasKeyStore());

    public static final StubIdpAppExtension applicationRule = new StubIdpAppExtension(Map.ofEntries(
            Map.entry("isHeadlessIdpEnabled", "true"),
            Map.entry("isPrometheusEnabled", "true"),
            Map.entry("isIdpEnabled", "false")))
            .withStubIdp(aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build());

    @BeforeEach
    public void refreshMetadata() {
        client.target("http://localhost:"+applicationRule.getAdminPort()+"/tasks/metadata-refresh").request().post(Entity.text(""));
    }

    @Test
    void headlessTest() {
        final String samlResponse = authnRequestSteps.userPostsAuthnRequestToHeadlessIdpReturnResponse(false, "relayState");
        samlResponseDecrypter.decryptSaml(samlResponse);
        zzz_checkMetrics();
    }

    public void zzz_checkMetrics() {
        Response response = client.target(UriBuilder.fromUri("http://localhost:" + applicationRule.getAdminPort())
                .path(PROMETHEUS_METRICS_RESOURCE)
                .build()).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        final String entity = response.readEntity(String.class);
        final List<String> metrics = List.of(entity.split(System.lineSeparator())).stream().filter(s -> s.startsWith("stubidp_")).collect(Collectors.toList());
        metricsContains(metrics, "stubidp_headless_receivedAuthnRequests_total 1.0");
        metricsContains(metrics, "stubidp_headless_successfulAuthnRequests_total 1.0");
        metricsContains(metrics, "stubidp_verify_sentAuthnResponses_success_total 0.0");
    }

    private void metricsContains(List<String> metrics, String metric) {
        assertThat(metrics.stream().anyMatch(m -> m.startsWith(metric))).withFailMessage(format("{0} not in {1}", metric, metrics)).isTrue();
    }
}
