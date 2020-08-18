package stubidp.kotlin.test.integration

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.assertj.core.api.Assertions
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.w3c.dom.Document
import org.w3c.dom.Node
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubidp.kotlin.test.integration.support.StubIdpBuilder
import stubidp.stubidp.Urls
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.UriBuilder

@ExtendWith(DropwizardExtensionsSupport::class)
class EidasCountryMetadataIntegrationTests : IntegrationTestHelper() {
    private val client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)

    @BeforeEach
    fun refreshMetadata() {
        client.target("http://localhost:" + applicationRule.adminPort + "/tasks/connector-metadata-refresh").request().post(Entity.text(""))
    }

    @Test
    fun countryMetadataShouldContainCorrectEntityIdAndSsoUrlTest() {
        val baseUrl = applicationRule.configuration?.europeanIdentityConfiguration?.stubCountryBaseUrl
        val metadataEndpoint = UriBuilder.fromUri(baseUrl + Urls.EIDAS_METADATA_RESOURCE).build(COUNTRY_NAME).toASCIIString()
        val expectedSsoUrl = UriBuilder.fromUri(baseUrl + Urls.EIDAS_SAML2_SSO_RESOURCE).build(COUNTRY_NAME).toASCIIString()
        val response = client.target("http://localhost:" + applicationRule.getPort(0) + UriBuilder.fromPath(Urls.EIDAS_METADATA_RESOURCE).build(COUNTRY_NAME).toASCIIString()).request().get()
        val metadata = response.readEntity(Document::class.java)
        val entityId = metadata.documentElement.attributes.getNamedItem("entityID").nodeValue
        val ssoUrl = getChildByTagName(getChildByTagName(metadata.documentElement, "md:IDPSSODescriptor"), "md:SingleSignOnService")!!.attributes.getNamedItem("Location").nodeValue
        Assertions.assertThat(response.status).isEqualTo(200)
        Assertions.assertThat(entityId).isEqualTo(metadataEndpoint)
        Assertions.assertThat(ssoUrl).isEqualTo(expectedSsoUrl)
    }

    private fun getChildByTagName(element: Node?, tagName: String): Node? {
        val children = element!!.childNodes
        for (i in 0 until children.length) {
            if (children.item(i).nodeName == tagName) {
                return children.item(i)
            }
        }
        return null
    }

    companion object {
        private const val COUNTRY_NAME = "stub-country"
        const val DISPLAY_NAME = "User Repository Identity Service"
        val applicationRule = StubIdpAppExtension(java.util.Map.ofEntries<String, String>(
                java.util.Map.entry<String, String>("europeanIdentity.enabled", "true"),
                java.util.Map.entry<String, String>("isIdpEnabled", "false")))
                .withStubIdp(StubIdpBuilder.aStubIdp()
                        .withId(COUNTRY_NAME)
                        .withDisplayName(DISPLAY_NAME)
                        .build())
    }
}