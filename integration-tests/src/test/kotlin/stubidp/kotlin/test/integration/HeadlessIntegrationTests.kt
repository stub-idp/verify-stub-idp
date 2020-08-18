package stubidp.kotlin.test.integration

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.assertj.core.api.Assertions
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import stubidp.kotlin.test.integration.steps.AuthnRequestSteps
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubidp.kotlin.test.integration.support.StubIdpBuilder
import stubidp.metrics.prometheus.bundle.PrometheusBundle
import stubidp.stubidp.resources.idp.HeadlessIdpResource
import stubsp.stubsp.saml.response.SamlResponseDecrypter
import java.text.MessageFormat
import java.util.Optional
import java.util.stream.Collectors
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.UriBuilder

@ExtendWith(DropwizardExtensionsSupport::class)
class HeadlessIntegrationTests : IntegrationTestHelper() {
    private val client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)
    private val authnRequestSteps = AuthnRequestSteps(
            client,
            IDP_NAME,
            applicationRule.localPort)
    private val samlResponseDecrypter = SamlResponseDecrypter(client,
            applicationRule.verifyMetadataPath,
            applicationRule.configuration?.metadataConfiguration?.expectedEntityId,
            Optional.empty(),
            applicationRule.assertionConsumerServices,
            applicationRule.hubKeyStore,
            applicationRule.eidasKeyStore)

    @BeforeEach
    fun refreshMetadata() {
        client.target("http://localhost:" + applicationRule.adminPort + "/tasks/metadata-refresh").request().post(Entity.text(""))
    }

    @Test
    fun headlessTest() {
        val samlResponse = authnRequestSteps.userPostsAuthnRequestToHeadlessIdpReturnResponse(false, "relayState")
        samlResponseDecrypter.decryptSaml(samlResponse)
        zzz_checkMetrics()
    }

    fun zzz_checkMetrics() {
        val response = client.target(UriBuilder.fromUri("http://localhost:" + applicationRule.adminPort)
                .path(PrometheusBundle.PROMETHEUS_METRICS_RESOURCE)
                .build()).request().get()
        Assertions.assertThat(response.status).isEqualTo(200)
        val entity = response.readEntity(String::class.java)
        val metrics = java.util.List.of(*entity.split(System.lineSeparator().toRegex()).toTypedArray()).stream().filter { s: String -> s.startsWith("stubidp_") }.collect(Collectors.toList())
        metricsContains(metrics, "stubidp_headless_receivedAuthnRequests_total 1.0")
        metricsContains(metrics, "stubidp_headless_successfulAuthnRequests_total 1.0")
        metricsContains(metrics, "stubidp_verify_sentAuthnResponses_success_total 0.0")
    }

    private fun metricsContains(metrics: List<String>, metric: String) {
        Assertions.assertThat(metrics.stream().anyMatch { m: String -> m.startsWith(metric) })
                .isTrue() // don't remove brackets
                .withFailMessage(MessageFormat.format("{0} not in {1}", metric, metrics))
    }

    companion object {
        private const val IDP_NAME = HeadlessIdpResource.IDP_NAME
        private const val DISPLAY_NAME = "User Login Identity Service"
        val applicationRule = StubIdpAppExtension(java.util.Map.ofEntries<String, String>(
                java.util.Map.entry<String, String>("isHeadlessIdpEnabled", "true"),
                java.util.Map.entry<String, String>("isPrometheusEnabled", "true"),
                java.util.Map.entry<String, String>("isIdpEnabled", "false")))
                .withStubIdp(StubIdpBuilder.aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build())
    }
}