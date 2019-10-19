package stubidp.test.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.hub.hub.domain.LevelOfAssurance;
import stubidp.stubidp.Urls;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.test.integration.steps.AuthnRequestSteps;
import stubidp.test.integration.support.IntegrationTestHelper;
import stubidp.test.integration.support.SamlDecrypter;
import stubidp.test.integration.support.StubIdpAppExtension;
import stubidp.test.integration.support.eidas.InboundResponseFromCountry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.stubidp.resources.eidas.EidasConsentResource.RSASHA_256;
import static stubidp.stubidp.resources.eidas.EidasConsentResource.RSASSA_PSS;

@ExtendWith(DropwizardExtensionsSupport.class)
public class EidasUserLogsInIntegrationTests extends IntegrationTestHelper {

    private final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);
    private final String EIDAS_SCHEME_NAME = EidasScheme.stub_cef_reference.getEidasSchemeName();
    private final AuthnRequestSteps authnRequestSteps = new AuthnRequestSteps(
            client,
            EIDAS_SCHEME_NAME,
            applicationRule.getLocalPort());
    private final SamlDecrypter samlDecrypter = new SamlDecrypter(client,
            applicationRule.getVerifyMetadataPath(),
            applicationRule.getConfiguration().getEuropeanIdentityConfiguration().getHubConnectorEntityId(),
            applicationRule.getLocalPort(),
            Optional.ofNullable(EIDAS_SCHEME_NAME),
            applicationRule.getAssertionConsumerServices());

    public static final StubIdpAppExtension applicationRule = new StubIdpAppExtension(Map.ofEntries(
            Map.entry("europeanIdentity.enabled", "true"),
            Map.entry("isIdpEnabled", "false")));

    @BeforeEach
    void refreshMetadata() {
        client.target("http://localhost:"+applicationRule.getAdminPort()+"/tasks/connector-metadata-refresh").request().post(Entity.text(""));
    }

    @Test
    void incorrectlySignedAuthnRequestFailsTest() {
        Response response = authnRequestSteps.userPostsEidasAuthnRequestReturnResponse(true, true, true, true);
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    void correctlySignedButMissingKeyInfoAuthnRequestFailsTest() {
        Response response = authnRequestSteps.userPostsEidasAuthnRequestReturnResponse(true, true, false);
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    void loginBehaviourTest() {
        final AuthnRequestSteps.Cookies cookies = authnRequestSteps.userPostsEidasAuthnRequestToStubIdp();
        authnRequestSteps.eidasUserLogsIn(cookies, true);
        authnRequestSteps.eidasUserConsentsReturnSamlResponse(cookies, false, RSASHA_256);
    }

    @Test
    void loginBehaviourTestPSS() {
        final AuthnRequestSteps.Cookies cookies = authnRequestSteps.userPostsEidasAuthnRequestToStubIdp();
        authnRequestSteps.eidasUserLogsIn(cookies, true);
        authnRequestSteps.eidasUserConsentsReturnSamlResponse(cookies, false, RSASSA_PSS);
    }

    @Test
    void debugPageLoadsAndValuesForOptionalAttribuesAreReturnedTest() {
        // this test requests these attributes and checks that they are displayed on the debug page as requested
        // but stub-country can't currently return any values for these attributes
        final boolean requestGender = true;
        final boolean requestAddress = true;
        final AuthnRequestSteps.Cookies cookies = authnRequestSteps.userPostsEidasAuthnRequestToStubIdpWithAttribute(requestAddress, requestGender);
        authnRequestSteps.eidasUserLogsIn(cookies, true);
        final String page = authnRequestSteps.eidasUserViewsTheDebugPage(cookies);
        // these can be requested but stub-country currently has no users that contain current address or gender
        if (requestAddress) { assertThat(page).contains(IdaConstants.Eidas_Attributes.CurrentAddress.NAME); }
        if (requestGender) { assertThat(page).contains(IdaConstants.Eidas_Attributes.Gender.NAME); }
        final String samlResponse = authnRequestSteps.eidasUserConsentsReturnSamlResponse(cookies, false, RSASHA_256);
        final InboundResponseFromCountry inboundResponseFromCountry = samlDecrypter.decryptEidasSaml(samlResponse);
        assertThat(inboundResponseFromCountry.getIssuer()).isEqualTo(UriBuilder.fromUri("http://localhost:0" + Urls.EIDAS_METADATA_RESOURCE).build(EIDAS_SCHEME_NAME).toASCIIString());
        assertThat(inboundResponseFromCountry.getStatus().getStatusCode().getValue()).isEqualTo(StatusCode.SUCCESS);
        assertThat(inboundResponseFromCountry.getValidatedIdentityAssertion().getAuthnStatements().size()).isEqualTo(1);
        assertThat(LevelOfAssurance.fromString(inboundResponseFromCountry.getValidatedIdentityAssertion().getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef())).isEqualTo(LevelOfAssurance.SUBSTANTIAL);
        assertThat(inboundResponseFromCountry.getValidatedIdentityAssertion().isSigned()).isTrue();
        assertThat(inboundResponseFromCountry.getValidatedIdentityAssertion().getAttributeStatements().size()).isEqualTo(1);
        final List<Attribute> attributes = inboundResponseFromCountry.getValidatedIdentityAssertion().getAttributeStatements().get(0).getAttributes();
        // stub-country can currently only return these 4 attributes
        assertThat(attributes.size()).isEqualTo(4);
        assertThat(attributes.stream().map(Attribute::getName).collect(Collectors.toList()))
                .containsExactlyInAnyOrder(
                        IdaConstants.Eidas_Attributes.FirstName.NAME,
                        IdaConstants.Eidas_Attributes.FamilyName.NAME,
                        IdaConstants.Eidas_Attributes.PersonIdentifier.NAME,
                        IdaConstants.Eidas_Attributes.DateOfBirth.NAME);
    }

    @Test
    void unsignedAssertionsAreReturnedWhenRequested() {
        final AuthnRequestSteps.Cookies cookies = authnRequestSteps.userPostsEidasAuthnRequestToStubIdp();
        authnRequestSteps.eidasUserLogsIn(cookies, false);
        final String samlResponse = authnRequestSteps.eidasUserConsentsReturnSamlResponse(cookies, false, RSASHA_256);
        final InboundResponseFromCountry inboundResponseFromCountry = samlDecrypter.decryptEidasSamlUnsignedAssertions(samlResponse);
        assertThat(inboundResponseFromCountry.getIssuer()).isEqualTo(UriBuilder.fromUri("http://localhost:0" + Urls.EIDAS_METADATA_RESOURCE).build(EIDAS_SCHEME_NAME).toASCIIString());
        assertThat(inboundResponseFromCountry.getStatus().getStatusCode().getValue()).isEqualTo(StatusCode.SUCCESS);
        assertThat(inboundResponseFromCountry.getValidatedIdentityAssertion().getAuthnStatements().size()).isEqualTo(1);
        assertThat(LevelOfAssurance.fromString(inboundResponseFromCountry.getValidatedIdentityAssertion().getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef())).isEqualTo(LevelOfAssurance.SUBSTANTIAL);
        assertThat(inboundResponseFromCountry.getValidatedIdentityAssertion().isSigned()).isFalse();
        assertThat(inboundResponseFromCountry.getValidatedIdentityAssertion().getAttributeStatements().size()).isEqualTo(1);
        final List<Attribute> attributes = inboundResponseFromCountry.getValidatedIdentityAssertion().getAttributeStatements().get(0).getAttributes();
        // stub-country can currently only return these 4 attributes
        assertThat(attributes.size()).isEqualTo(4);
        assertThat(attributes.stream().map(Attribute::getName).collect(Collectors.toList()))
                .containsExactlyInAnyOrder(
                        IdaConstants.Eidas_Attributes.FirstName.NAME,
                        IdaConstants.Eidas_Attributes.FamilyName.NAME,
                        IdaConstants.Eidas_Attributes.PersonIdentifier.NAME,
                        IdaConstants.Eidas_Attributes.DateOfBirth.NAME);
    }
}
