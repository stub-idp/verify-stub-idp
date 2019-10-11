package stubidp.test.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.stubidp.Urls;
import stubidp.stubidp.domain.SubmitButtonValue;
import stubidp.test.integration.steps.FormBuilder;
import stubidp.test.integration.steps.PreRegistrationSteps;
import stubidp.test.integration.support.IntegrationTestHelper;
import stubidp.test.integration.support.StubIdpAppExtension;
import stubidp.test.integration.support.IdpAuthnRequestBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

import static stubidp.stubidp.builders.StubIdpBuilder.aStubIdp;
import static stubidp.stubidp.csrf.AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY;

@ExtendWith(DropwizardExtensionsSupport.class)
public class PreRegistrationIntegrationTest extends IntegrationTestHelper {

    private static final String IDP_NAME = "stub-idp-demo-one";
    private static final String DISPLAY_NAME = "Stub Idp One Pre-Register";
    private static final String FIRSTNAME_PARAM = "Jack";
    private static final String SURNAME_PARAM = "Bauer";
    private static final String ADDRESS_LINE1_PARAM = "123 Letsbe Avenue";
    private static final String ADDRESS_LINE2_PARAM = "Somewhere";
    private static final String ADDRESS_TOWN_PARAM = "Smallville";
    private static final String ADDRESS_POST_CODE_PARAM = "VE7 1FY";
    private static final String DATE_OF_BIRTH_PARAM = "1981-06-06";
    private static final String USERNAME_PARAM = "pre-registering-user";
    private static final String PASSWORD_PARAM = "bar";
    private static final String LEVEL_OF_ASSURANCE_PARAM = AuthnContext.LEVEL_2.name();

    public static final StubIdpAppExtension applicationRule = new StubIdpAppExtension(Map.of("singleIdpJourney.enabled", "true"))
            .withStubIdp(aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build());

    public static Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    @BeforeEach
    public void setUp() {
        client.target("http://localhost:" + applicationRule.getAdminPort() + "/tasks/metadata-refresh").request().post(Entity.text(""));
    }

    @Test
    public void userPreRegistersAndThenComesFromRP(){
        PreRegistrationSteps steps = new PreRegistrationSteps(client, applicationRule);

        steps

        .userSuccessfullyNavigatesTo(Urls.SINGLE_IDP_PRE_REGISTER_RESOURCE)
        .responseContains("Register with " + DISPLAY_NAME)

        .userSubmitsForm(
            FormBuilder.newForm()
                .withParam(Urls.IDP_ID_PARAM, IDP_NAME)
                .withParam(Urls.FIRSTNAME_PARAM, FIRSTNAME_PARAM)
                .withParam(Urls.SURNAME_PARAM, SURNAME_PARAM)
                .withParam(Urls.ADDRESS_LINE1_PARAM, ADDRESS_LINE1_PARAM)
                .withParam(Urls.ADDRESS_LINE2_PARAM, ADDRESS_LINE2_PARAM)
                .withParam(Urls.ADDRESS_TOWN_PARAM, ADDRESS_TOWN_PARAM)
                .withParam(Urls.ADDRESS_POST_CODE_PARAM, ADDRESS_POST_CODE_PARAM)
                .withParam(Urls.DATE_OF_BIRTH_PARAM, DATE_OF_BIRTH_PARAM)
                .withParam(Urls.USERNAME_PARAM, USERNAME_PARAM)
                .withParam(Urls.PASSWORD_PARAM, PASSWORD_PARAM)
                .withParam(Urls.LEVEL_OF_ASSURANCE_PARAM, LEVEL_OF_ASSURANCE_PARAM)
                .withParam(CSRF_PROTECT_FORM_KEY, steps.getCsrfToken())
                .withParam(Urls.SUBMIT_PARAM, SubmitButtonValue.Register.toString())
                .build(),
                Urls.IDP_REGISTER_RESOURCE)
        .userIsRedirectedTo(Urls.SINGLE_IDP_START_PROMPT_RESOURCE +"?source=pre-reg")
        .theRedirectIsFollowed()
        .theResponseStatusIs(Response.Status.OK)
        .responseContains(FIRSTNAME_PARAM,
                            SURNAME_PARAM,
                            ADDRESS_LINE1_PARAM,
                            ADDRESS_LINE2_PARAM,
                            ADDRESS_POST_CODE_PARAM,
                            LEVEL_OF_ASSURANCE_PARAM)
        // ... hub ...

        // Simulate Authn Request from hub
        .clientPostsFormData(FormBuilder.newForm()
                                .withParam(Urls.SAML_REQUEST_PARAM, IdpAuthnRequestBuilder.anAuthnRequest().withDestination(UriBuilder.fromUri("http://localhost:0"+Urls.IDP_SAML2_SSO_RESOURCE).build(IDP_NAME).toASCIIString()).build())
                                .withParam(Urls.RELAY_STATE_PARAM, "relay-state")
                                .build(),
                        Urls.IDP_SAML2_SSO_RESOURCE)

        .userIsRedirectedTo(Urls.IDP_LOGIN_RESOURCE)
        .theRedirectIsFollowed()
        .userIsRedirectedTo(Urls.IDP_CONSENT_RESOURCE)
        .theRedirectIsFollowed()
        .theResponseStatusIs(Response.Status.OK)
        .responseContains(FIRSTNAME_PARAM,
                            SURNAME_PARAM,
                            ADDRESS_LINE1_PARAM,
                            ADDRESS_LINE2_PARAM,
                            ADDRESS_POST_CODE_PARAM,
                            LEVEL_OF_ASSURANCE_PARAM);
    }
}
