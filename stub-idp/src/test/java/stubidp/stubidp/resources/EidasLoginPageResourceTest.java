package stubidp.stubidp.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.shared.cookies.CookieNames;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.shared.views.SamlRedirectView;
import stubidp.stubidp.Urls;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.EidasAuthnRequest;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.domain.SamlResponseFromValue;
import stubidp.stubidp.exceptions.GenericStubIdpException;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountry;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.stubidp.resources.eidas.EidasLoginPageResource;
import stubidp.stubidp.services.EidasAuthnResponseService;
import stubidp.stubidp.services.StubCountryService;
import stubidp.utils.rest.common.SessionId;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EidasLoginPageResourceTest {

    private EidasLoginPageResource resource;
    private EidasSession session;
    
    private final String SCHEME_NAME = EidasScheme.stub_country.getEidasSchemeName();
    private final SessionId SESSION_ID = SessionId.createNewSessionId();
    private final String USERNAME = "username";
    private final String PASSWORD = "password";

    @Mock
    private EidasSessionRepository sessionRepository;

    @Mock
	private EidasAuthnResponseService eidasSuccessAuthnResponseService;

    @Mock
    private SamlResponseFromValue<org.opensaml.saml.saml2.core.Response> samlResponse;

    @Mock
    private StubCountryRepository stubCountryRepository;

    @Mock
    private StubCountry stubCountry;

    @Mock
    private DatabaseIdpUser user;

    @Mock
    private StubCountryService stubCountryService;
    @Mock
    private CookieNames cookieNames;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        SamlMessageRedirectViewFactory samlResponseRedirectViewFactory = new SamlMessageRedirectViewFactory(cookieNames);
        resource = new EidasLoginPageResource(sessionRepository, eidasSuccessAuthnResponseService, samlResponseRedirectViewFactory, stubCountryRepository, stubCountryService);
        EidasAuthnRequest eidasAuthnRequest = new EidasAuthnRequest("request-id", "issuer", "destination", "loa", Collections.emptyList());
        session = new EidasSession(SESSION_ID, eidasAuthnRequest, null, null, null, Optional.empty(), Optional.empty());
    }

    @Test
    public void loginShouldRedirectToEidasConsentResource() {
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.ofNullable(session));
        final Response response = resource.post(SCHEME_NAME, USERNAME, PASSWORD, SESSION_ID);

        assertThat(response.getLocation()).isEqualTo(UriBuilder.fromPath(Urls.EIDAS_CONSENT_RESOURCE)
                .build(SCHEME_NAME));
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
    }

    @Test
    public void loginShouldReturnASuccessfulResponse() {
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.ofNullable(session));
        when(stubCountryRepository.getStubCountryWithFriendlyId(EidasScheme.fromString(SCHEME_NAME).get())).thenReturn(stubCountry);

        final Response response = resource.get(SCHEME_NAME, Optional.empty(), SESSION_ID);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void authnFailShouldReturnAnUnsuccessfulResponse() throws URISyntaxException {
        when(sessionRepository.deleteAndGet(SESSION_ID)).thenReturn(Optional.ofNullable(session));
        when(eidasSuccessAuthnResponseService.generateAuthnFailed(session, SCHEME_NAME)).thenReturn(samlResponse);
        when(samlResponse.getResponseString()).thenReturn("<saml2p:Response/>");
        when(samlResponse.getSpSSOUrl()).thenReturn(new URI("http://hub.url/"));
        when(cookieNames.getSessionCookieName()).thenReturn("sessionCookieName");

        final Response response = resource.postAuthnFailure(SCHEME_NAME, SESSION_ID);

        verify(eidasSuccessAuthnResponseService).generateAuthnFailed(session, SCHEME_NAME);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(SamlRedirectView.class);
        SamlRedirectView returnedPage = (SamlRedirectView)response.getEntity();
        assertThat(returnedPage.getTargetUri().toString()).isEqualTo("http://hub.url/");
    }

    @Test
    public void loginShouldThrowAGenericStubIdpExceptionWhenSessionIsEmpty() {
        Assertions.assertThrows(GenericStubIdpException.class,
                () -> resource.get(SCHEME_NAME, Optional.empty(), null));
    }
}
