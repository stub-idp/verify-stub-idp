package stubsp.stubsp;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.domain.assertions.IpAddress;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.domain.assertions.TransliterableMdsValue;
import stubidp.saml.domain.response.OutboundResponseFromIdp;
import stubidp.utils.security.security.IdGenerator;
import stubsp.stubsp.domain.AvailableServiceDto;
import stubsp.stubsp.integration.support.StubSpAppExtension;
import stubsp.stubsp.saml.IdpSamlGenerator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static stubsp.stubsp.integration.support.StubSpAppExtension.SP_ENTITY_ID;

@ExtendWith(DropwizardExtensionsSupport.class)
public class StubSpApplicationIntegrationTest {

    private static final StubSpAppExtension stubSpAppExtension = new StubSpAppExtension();
    private static final IdGenerator idGenerator = new IdGenerator();
    private final IdpSamlGenerator idpSamlGenerator = new IdpSamlGenerator(stubSpAppExtension.getSpEncryptionTrustStore(),
            stubSpAppExtension.getIdpSigningKeyStore(),
            x -> SP_ENTITY_ID,
            new SignatureRSASHA256(),
            new DigestSHA256());

    private final Client client = JerseyClientBuilder.createClient()
            .property(ClientProperties.FOLLOW_REDIRECTS, false);

    @BeforeEach
    void refreshMetadata() {
        client.target("http://localhost:"+stubSpAppExtension.getAdminPort()+"/tasks/idp-metadata-refresh")
                .request()
                .post(Entity.text(""));
    }

    @Test
    void testMainPage() {
        final Response response = get(Urls.ROOT_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("<a href=\"/stub/sp/secure\">Secure Area</a>");
    }

    @Test
    void testSecureResourceNeedsLogin() {
        Response response = get(Urls.SECURE_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("Saml Processing...");
        Response response2 = get("/assets/shared/scripts/saml-redirect-auto-submit.js");
        assertThat(response2.getStatus()).isEqualTo(200);
        assertThat(response2.readEntity(String.class)).contains("submit()");
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

    @Test
    void testValidSamlResponseIsAccepted() {
        Response response = get(Urls.SECURE_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        String requestForm = response.readEntity(String.class);
        assertThat(requestForm).contains("Saml Processing...");
        String samlRequest = getSamlResponseFromResponseString(requestForm);
        String relayState = getRelayStateFromResponseHtml(requestForm);
        String inResponseTo = "inResponseTo";
        PersistentId persistentId = new PersistentId(UUID.randomUUID().toString());
        AssertionRestrictions assertionRestrictions = new AssertionRestrictions(Instant.now().plus(5, ChronoUnit.MINUTES), inResponseTo, SP_ENTITY_ID);
        MatchingDataset matchingDataset = new MatchingDataset(List.of(new TransliterableMdsValue("Kermit", "Kermit")), List.of(), List.of(), Optional.empty(), List.of(), List.of(), List.of(), persistentId.getNameId());
        IdentityProviderAssertion matchingDatasetAssertion = new IdentityProviderAssertion(idGenerator.getId(), StubSpAppExtension.IDP_ENTITY_ID, Instant.now(), persistentId, assertionRestrictions, Optional.of(matchingDataset), Optional.empty());
        IdentityProviderAuthnStatement identityProviderAuthnStatement = IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement(AuthnContext.LEVEL_2, new IpAddress("127.0.0.1"));
        IdentityProviderAssertion authnStatementAssertion = new IdentityProviderAssertion(idGenerator.getId(), StubSpAppExtension.IDP_ENTITY_ID, Instant.now(), persistentId, assertionRestrictions, Optional.empty(), Optional.of(identityProviderAuthnStatement));
        OutboundResponseFromIdp outboundResponseFromIdp = OutboundResponseFromIdp.createSuccessResponseFromIdp(idGenerator.getId(),
                inResponseTo,
                StubSpAppExtension.IDP_ENTITY_ID,
                matchingDatasetAssertion,
                authnStatementAssertion,
                StubSpAppExtension.getExpectedDestination());
        String samlResponse = idpSamlGenerator.generate(outboundResponseFromIdp);
        Response response2 = post(Urls.SAML_SSO_RESPONSE_RESOURCE, samlResponse, relayState);
        assertThat(response2.getStatus()).isEqualTo(303);
        assertThat(response2.getLocation().getPath()).isEqualTo(Urls.SUCCESS_RESOURCE);
        Response response3 = get(Urls.SUCCESS_RESOURCE);
        assertThat(response3.getStatus()).isEqualTo(200);
        assertThat(response3.readEntity(String.class)).contains("You signed in successfully");
    }

    @Test
    void testInvalidSamlResponseIsRejected() {
        Response response = get(Urls.SECURE_RESOURCE);
        assertThat(response.getStatus()).isEqualTo(200);
        String requestForm = response.readEntity(String.class);
        assertThat(requestForm).contains("Saml Processing...");
        String samlRequest = getSamlResponseFromResponseString(requestForm);
        String relayState = getRelayStateFromResponseHtml(requestForm);
        String inResponseTo = "inResponseTo";
        OutboundResponseFromIdp outboundResponseFromIdp = OutboundResponseFromIdp.createAuthnFailedResponseIssuedByIdp(idGenerator.getId(),
                inResponseTo,
                StubSpAppExtension.IDP_ENTITY_ID,
                StubSpAppExtension.getExpectedDestination());
        String samlResponse = idpSamlGenerator.generate(outboundResponseFromIdp);
        Response response2 = post(Urls.SAML_SSO_RESPONSE_RESOURCE, samlResponse, relayState);
        assertThat(response2.getStatus()).isEqualTo(303);
        assertThat(response2.getLocation().getPath()).isEqualTo(Urls.AUTHENTICATION_FAILURE_RESOURCE);
        Response response3 = get(Urls.AUTHENTICATION_FAILURE_RESOURCE);
        assertThat(response3.getStatus()).isEqualTo(200);
        assertThat(response3.readEntity(String.class)).contains("You failed to sign-in successfully");
    }

    public String getSamlResponseFromResponseString(String responseString) {
        final Document page = Jsoup.parse(responseString);
        assertThat(page.getElementsByTag("title").text()).isEqualTo("Saml Processing...");
        return page.getElementsByAttributeValue("name", "SAMLResponse").val();
    }

    private String getRelayStateFromResponseHtml(String entityString) {
        final Document page = Jsoup.parse(entityString);
        assertThat(page.getElementsByTag("title").text()).isEqualTo("Saml Processing...");
        final Element relayStateElement = page.getElementById(Urls.RELAY_STATE_PARAM);
        return relayStateElement.val();
    }

    private Response get(String resource) {
        return client.target(UriBuilder.fromUri("http://localhost:" + stubSpAppExtension.getLocalPort() + resource).build())
                .request()
                .get();
    }

    private Response post(String resource, String samlResponse, String relayState) {
        Form form = new Form();
        form.param(Urls.SAML_RESPONSE_PARAM, samlResponse);
        form.param(Urls.RELAY_STATE_PARAM, relayState);
        return client.target(UriBuilder.fromUri("http://localhost:" + stubSpAppExtension.getLocalPort() + resource).build())
                .request()
                .post(Entity.form(form));
    }
}
