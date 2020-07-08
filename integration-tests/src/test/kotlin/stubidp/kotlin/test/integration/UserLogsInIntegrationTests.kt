package stubidp.kotlin.test.integration

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.assertj.core.api.Assertions
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import stubidp.metrics.prometheus.bundle.PrometheusBundle
import stubidp.saml.constants.Constants
import stubidp.stubidp.Urls
import stubidp.stubidp.builders.StubIdpBuilder
import stubidp.stubidp.cookies.StubIdpCookieNames
import stubidp.kotlin.test.integration.steps.AuthnRequestSteps
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubidp.utils.rest.common.HttpHeaders
import stubsp.stubsp.saml.response.SamlResponseDecrypter
import java.text.MessageFormat
import java.util.Optional
import java.util.UUID
import java.util.stream.Collectors
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.UriBuilder

@ExtendWith(DropwizardExtensionsSupport::class)
class UserLogsInIntegrationTests : IntegrationTestHelper() {
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
            applicationRule.eidasKeyStore,
            checkKeyInfo)

    @BeforeEach
    fun refreshMetadata() {
        client.target("http://localhost:" + applicationRule.adminPort + "/tasks/metadata-refresh").request().post(Entity.text(""))
    }

    //    @Test
    //    @Order(Integer.MAX_VALUE)
    fun zzz_checkMetrics() {
        val response = client.target(UriBuilder.fromUri("http://localhost:" + applicationRule.adminPort)
                .path(PrometheusBundle.PROMETHEUS_METRICS_RESOURCE)
                .build()).request().get()
        Assertions.assertThat(response.status).isEqualTo(200)
        val entity = response.readEntity(String::class.java)
        val metrics = java.util.List.of(*entity.split(System.lineSeparator().toRegex()).toTypedArray()).stream().filter { s: String -> s.startsWith("stubidp_") }.collect(Collectors.toList())
        metricsContains(metrics, "stubidp_verify_receivedAuthnRequests_total 7.0")
        //        metricsContains(metrics, "stubidp_eidas_receivedAuthnRequests_total");
        metricsContains(metrics, "stubidp_verify_successfulAuthnRequests_total 6.0")
        metricsContains(metrics, "stubidp_eidas_successfulAuthnRequests_total 0.0")
        metricsContains(metrics, "stubidp_verify_sentAuthnResponses_success_total 4.0")
        //        metricsContains(metrics, "stubidp_eidas_sentAuthnResponses_success_total 0.0");
        metricsContains(metrics, "stubidp_verify_invalid_AuthnRequests_received_total 1.0")
        metricsContains(metrics, "stubidp_eidas_invalid_AuthnRequests_received_total 0.0")
        metricsContains(metrics, "stubidp_verify_sentAuthnResponses_failure_total{failure_type=\"fraud\",} 1.0")
        metricsContains(metrics, "stubidp_db_users_total 12.0")
        metricsContains(metrics, "stubidp_db_sessions_total 1.0")
        metricsContains(metrics, "stubidp_replay_cache_total")
    }

    private fun metricsContains(metrics: List<String>, metric: String) {
        Assertions.assertThat(metrics.stream().anyMatch { m: String -> m.startsWith(metric) }).withFailMessage(MessageFormat.format("{0} not in {1}", metric, metrics)).isTrue()
    }

    @Test
    @Order(1)
    fun incorrectlySignedAuthnRequestFailsTest() {
        val response = authnRequestSteps.userPostsAuthnRequestToStubIdpReturnResponse(java.util.List.of(), Optional.empty(), Optional.empty(), true, Optional.empty())
        Assertions.assertThat(response.status).isEqualTo(500)
    }

    @Test
    @Order(1)
    fun loginBehaviourTest() {
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp()
        authnRequestSteps.userLogsIn(cookies)
        authnRequestSteps.userConsentsReturnSamlResponse(cookies, false)
    }

    @Test
    @Order(Int.MAX_VALUE)
    fun failureBehaviourTest() {
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp()
        val response = authnRequestSteps.userFailureFraud(cookies)
        zzz_checkMetrics()
    }

    @Test
    @Order(1)
    @Disabled
    @Throws(InterruptedException::class)
    fun testStaleSessionReaper() {
        // set times in StaleSessionReaperConfiguration to 1s, run this test and check the log lines
        for (i in 0..9) {
            authnRequestSteps.userPostsAuthnRequestToStubIdp()
            Thread.sleep(1000)
        }
    }

    @Test
    @Order(1)
    fun debugPageLoadsAndRelayStateIsCorrectlyDisplayedAndReturnedTest() {
        val relayState = UUID.randomUUID().toString()
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp(Optional.of(relayState))
        authnRequestSteps.userLogsIn(cookies)
        val debugPage = authnRequestSteps.userViewsTheDebugPage(cookies)
        Assertions.assertThat(debugPage).contains(MessageFormat.format("Relay state is \"{0}\"", relayState))
        val response = authnRequestSteps.userConsentsReturnResponse(cookies, false)
        val relayStateInResponse = authnRequestSteps.getRelayStateFromResponseHtml(response.readEntity(String::class.java))
        Assertions.assertThat(relayStateInResponse).isEqualTo(relayState)
    }

    @Test
    @Order(1)
    fun ensureImagesAreCacheableTest() {
        val response = client.target(authnRequestSteps.getStubIdpUri("/assets/images/providers/stub-idp-one.png"))
                .request()
                .get()
        Assertions.assertThat(response.status).isEqualTo(200)
        // ensure data can be stored by browser
        Assertions.assertThat(response.getHeaderString(HttpHeaders.CACHE_CONTROL_KEY)).isNull()
        Assertions.assertThat(response.getHeaderString(HttpHeaders.PRAGMA_KEY)).isNull()
    }

    @Test
    @Order(1)
    fun idpNotFoundTest() {
        val cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp()
        val response = client.target(authnRequestSteps.getStubIdpUri(UriBuilder.fromPath(Urls.IDP_LOGIN_RESOURCE).build("idp_that_does_not_exist").toString()))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.sessionId)
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.secure)
                .get()
        Assertions.assertThat(response.status).isEqualTo(404)
        val body = response.readEntity(String::class.java)
        Assertions.assertThat(body).contains("No idp found with friendlyId: idp_that_does_not_exist")
    }

    @Test
    @Order(1)
    fun randomizedPidTest() {
        val cookies1 = authnRequestSteps.userPostsAuthnRequestToStubIdp()
        authnRequestSteps.userLogsIn(cookies1)
        val samlResponse = authnRequestSteps.userConsentsReturnSamlResponse(cookies1, false)
        val inboundResponseFromIdp = samlResponseDecrypter.decryptSaml(samlResponse)
        val firstPid = inboundResponseFromIdp.authnStatementAssertion.get().persistentId.nameId
        val cookies2 = authnRequestSteps.userPostsAuthnRequestToStubIdp()
        authnRequestSteps.userLogsIn(cookies2)
        val samlResponse2 = authnRequestSteps.userConsentsReturnSamlResponse(cookies2, true)
        val inboundResponseFromIdp2 = samlResponseDecrypter.decryptSaml(samlResponse2)
        Assertions.assertThat(inboundResponseFromIdp2.authnStatementAssertion.get().persistentId.nameId).isNotEqualTo(firstPid)
    }

    @Test
    fun shouldGenerateIdpMetadataTest() {
        val response = client.target(authnRequestSteps.getStubIdpUri(UriBuilder.fromPath(Urls.IDP_METADATA_RESOURCE).build(IDP_NAME).toString()))
                .request()
                .get()
        Assertions.assertThat(response.status).isEqualTo(200)
        Assertions.assertThat(response.mediaType.toString()).isEqualTo(Constants.APPLICATION_SAMLMETADATA_XML)
        val body = response.readEntity(String::class.java)
        Assertions.assertThat(body).contains("IDPSSODescriptor")
    }

    companion object {
        // Use stub-idp-one as it allows us to use the defaultMetadata in MetadataFactory
        private const val IDP_NAME = "stub-idp-one"
        private const val DISPLAY_NAME = "User Login Identity Service"
        private const val checkKeyInfo = true
        val applicationRule = StubIdpAppExtension(java.util.Map.ofEntries<String, String>(java.util.Map.entry<String, String>("isPrometheusEnabled", "true")))
                .withStubIdp(StubIdpBuilder.aStubIdp()
                        .withId(IDP_NAME)
                        .withDisplayName(DISPLAY_NAME)
                        .sendKeyInfo(checkKeyInfo)
                        .build())
    }
}