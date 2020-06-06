package stubidp.kotlin.test.integration

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.assertj.core.api.Assertions
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.opensaml.saml.saml2.core.Attribute
import org.opensaml.saml.saml2.core.StatusCode
import stubidp.saml.extensions.IdaConstants
import stubidp.saml.hub.hub.domain.LevelOfAssurance
import stubidp.stubidp.Urls
import stubidp.stubidp.domain.EidasScheme
import stubidp.stubidp.resources.eidas.EidasConsentResource
import stubidp.kotlin.test.integration.steps.AuthnRequestSteps
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubsp.stubsp.saml.response.SamlResponseDecrypter
import java.text.MessageFormat
import java.util.Optional
import java.util.UUID
import java.util.stream.Collectors
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.UriBuilder

@ExtendWith(DropwizardExtensionsSupport::class)
class EidasUserLogsInIntegrationTests : IntegrationTestHelper() {
    private val client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)
    private val EIDAS_SCHEME_NAME = EidasScheme.stub_cef_reference.eidasSchemeName
    private val authnRequestSteps = AuthnRequestSteps(
            client,
            EIDAS_SCHEME_NAME,
            applicationRule.localPort)
    private val samlResponseDecrypter = SamlResponseDecrypter(client,
            applicationRule.verifyMetadataPath,
            applicationRule.configuration?.europeanIdentityConfiguration?.hubConnectorEntityId,
            Optional.ofNullable(UriBuilder.fromUri("http://localhost:" + applicationRule.localPort + Urls.EIDAS_METADATA_RESOURCE).build(EIDAS_SCHEME_NAME)),
            applicationRule.assertionConsumerServices,
            applicationRule.hubKeyStore,
            applicationRule.eidasKeyStore)

    @BeforeEach
    fun refreshMetadata() {
        client.target("http://localhost:" + applicationRule.adminPort + "/tasks/connector-metadata-refresh").request().post(Entity.text(""))
    }

    @Test
    fun incorrectlySignedAuthnRequestFailsTest() {
        val response = authnRequestSteps.userPostsEidasAuthnRequestReturnResponse(true, true, true, true, Optional.empty())
        Assertions.assertThat(response.status).isEqualTo(500)
    }

    @Test
    fun correctlySignedButMissingKeyInfoAuthnRequestFailsTest() {
        val response = authnRequestSteps.userPostsEidasAuthnRequestReturnResponse(true, true, false, Optional.empty())
        Assertions.assertThat(response.status).isEqualTo(500)
    }

    @Test
    fun loginBehaviourTest() {
        val cookies = authnRequestSteps.userPostsEidasAuthnRequestToStubIdp()
        authnRequestSteps.eidasUserLogsIn(cookies, true)
        authnRequestSteps.eidasUserConsentsReturnSamlResponse(cookies, false, EidasConsentResource.RSASHA_256)
    }

    @Test
    fun loginBehaviourTestPSSTest() {
        val cookies = authnRequestSteps.userPostsEidasAuthnRequestToStubIdp()
        authnRequestSteps.eidasUserLogsIn(cookies, true)
        authnRequestSteps.eidasUserConsentsReturnSamlResponse(cookies, false, EidasConsentResource.RSASSA_PSS)
    }

    @Test
    fun debugPageLoadsAndValuesForOptionalAttribuesAreReturnedTest() {
        // this test requests these attributes and checks that they are displayed on the debug page as requested
        // but stub-country can't currently return any values for these attributes
        val requestGender = true
        val requestAddress = true
        val relayState = UUID.randomUUID().toString()
        val cookies = authnRequestSteps.userPostsEidasAuthnRequestToStubIdpWithAttribute(requestAddress, requestGender, Optional.ofNullable(relayState))
        authnRequestSteps.eidasUserLogsIn(cookies, true)
        val debugPage = authnRequestSteps.eidasUserViewsTheDebugPage(cookies)
        // these can be requested but stub-country currently has no users that contain current address or gender
        if (requestAddress) {
            Assertions.assertThat(debugPage).contains(IdaConstants.Eidas_Attributes.CurrentAddress.NAME)
        }
        if (requestGender) {
            Assertions.assertThat(debugPage).contains(IdaConstants.Eidas_Attributes.Gender.NAME)
        }
        Assertions.assertThat(debugPage).contains(MessageFormat.format("Relay state is \"{0}\"", relayState))
        val response = authnRequestSteps.eidasUserConsentsReturnResponse(cookies, false, EidasConsentResource.RSASHA_256)
        val responseBody = response.readEntity(String::class.java)
        val relayStateResponse = authnRequestSteps.getRelayStateFromResponseHtml(responseBody)
        Assertions.assertThat(relayStateResponse).isEqualTo(relayState)
        val samlResponse = authnRequestSteps.getSamlResponseFromResponseString(responseBody)
        val inboundResponseFromCountry = samlResponseDecrypter.decryptEidasSaml(samlResponse)
        Assertions.assertThat(inboundResponseFromCountry.issuer).isEqualTo(UriBuilder.fromUri("http://localhost:0" + Urls.EIDAS_METADATA_RESOURCE).build(EIDAS_SCHEME_NAME).toASCIIString())
        Assertions.assertThat(inboundResponseFromCountry.status.statusCode.value).isEqualTo(StatusCode.SUCCESS)
        Assertions.assertThat(inboundResponseFromCountry.validatedIdentityAssertion.authnStatements.size).isEqualTo(1)
        Assertions.assertThat(LevelOfAssurance.fromString(inboundResponseFromCountry.validatedIdentityAssertion.authnStatements[0].authnContext.authnContextClassRef.uri)).isEqualTo(LevelOfAssurance.SUBSTANTIAL)
        Assertions.assertThat(inboundResponseFromCountry.validatedIdentityAssertion.isSigned).isTrue()
        Assertions.assertThat(inboundResponseFromCountry.validatedIdentityAssertion.attributeStatements.size).isEqualTo(1)
        val attributes = inboundResponseFromCountry.validatedIdentityAssertion.attributeStatements[0].attributes
        // stub-country can currently only return these 4 attributes
        Assertions.assertThat(attributes.size).isEqualTo(4)
        val list = attributes.stream().map { obj: Attribute -> obj.name }.collect(Collectors.toList())
        Assertions.assertThat(list)
                .containsExactlyInAnyOrder(
                        IdaConstants.Eidas_Attributes.FirstName.NAME,
                        IdaConstants.Eidas_Attributes.FamilyName.NAME,
                        IdaConstants.Eidas_Attributes.PersonIdentifier.NAME,
                        IdaConstants.Eidas_Attributes.DateOfBirth.NAME)
    }

    @Test
    fun unsignedAssertionsAreReturnedWhenRequestedTest() {
        val cookies = authnRequestSteps.userPostsEidasAuthnRequestToStubIdp()
        authnRequestSteps.eidasUserLogsIn(cookies, false)
        val samlResponse = authnRequestSteps.eidasUserConsentsReturnSamlResponse(cookies, false, EidasConsentResource.RSASHA_256)
        val inboundResponseFromCountry = samlResponseDecrypter.decryptEidasSamlUnsignedAssertions(samlResponse)
        Assertions.assertThat(inboundResponseFromCountry.issuer).isEqualTo(UriBuilder.fromUri("http://localhost:0" + Urls.EIDAS_METADATA_RESOURCE).build(EIDAS_SCHEME_NAME).toASCIIString())
        Assertions.assertThat(inboundResponseFromCountry.status.statusCode.value).isEqualTo(StatusCode.SUCCESS)
        Assertions.assertThat(inboundResponseFromCountry.validatedIdentityAssertion.authnStatements.size).isEqualTo(1)
        Assertions.assertThat(LevelOfAssurance.fromString(inboundResponseFromCountry.validatedIdentityAssertion.authnStatements[0].authnContext.authnContextClassRef.uri)).isEqualTo(LevelOfAssurance.SUBSTANTIAL)
        Assertions.assertThat(inboundResponseFromCountry.validatedIdentityAssertion.isSigned).isFalse()
        Assertions.assertThat(inboundResponseFromCountry.validatedIdentityAssertion.attributeStatements.size).isEqualTo(1)
        val attributes = inboundResponseFromCountry.validatedIdentityAssertion.attributeStatements[0].attributes
        // stub-country can currently only return these 4 attributes
        Assertions.assertThat(attributes.size).isEqualTo(4)
        val list = attributes.stream().map { obj: Attribute -> obj.name }.collect(Collectors.toList())
        Assertions.assertThat(list)
                .containsExactlyInAnyOrder(
                        IdaConstants.Eidas_Attributes.FirstName.NAME,
                        IdaConstants.Eidas_Attributes.FamilyName.NAME,
                        IdaConstants.Eidas_Attributes.PersonIdentifier.NAME,
                        IdaConstants.Eidas_Attributes.DateOfBirth.NAME)
    }

    companion object {
        val applicationRule = StubIdpAppExtension(java.util.Map.ofEntries<String, String>(
                java.util.Map.entry<String, String>("europeanIdentity.enabled", "true"),
                java.util.Map.entry<String, String>("isIdpEnabled", "false")))
    }
}