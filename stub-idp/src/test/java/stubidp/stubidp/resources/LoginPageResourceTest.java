package stubidp.stubidp.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.utils.hub.domain.IdaAuthnRequestFromHub;
import stubidp.shared.cookies.CookieFactory;
import stubidp.shared.cookies.CookieNames;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.SamlResponseFromValue;
import stubidp.stubidp.domain.SubmitButtonValue;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.repositories.AllIdpsUserRepository;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.resources.idp.LoginPageResource;
import stubidp.stubidp.services.IdpUserService;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.views.ErrorMessageType;
import stubidp.utils.rest.common.SessionId;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginPageResourceTest {

    private final String SAML_REQUEST_ID = "samlRequestId";
    private final String IDP_NAME = "an idp name";
    private final SessionId SESSION_ID = SessionId.createNewSessionId();
    private final String RELAY_STATE = "relayState";
    private final String USERNAME = "username";
    private final String PASSWORD = "password";

    private LoginPageResource resource;

    @Mock
    private IdpStubsRepository idpStubsRepository;
    @Mock
    private IdpSessionRepository sessionRepository;
    @Mock
    private NonSuccessAuthnResponseService nonSuccessAuthnResponseService;
    @Mock
    IdaAuthnRequestFromHub idaAuthnRequestFromHub;
    @Mock
    private IdpUserService idpUserService;
    @Mock
    private AllIdpsUserRepository allIdpsUserRepository;
    @Mock
    private DatabaseIdpUser databaseIdpUser;
    @Mock
    private Idp idp;
    @Mock
    private CookieFactory cookieFactory;
    @Mock
    private CookieNames cookieNames;

    @BeforeEach
    public void createResource() {
        resource = new LoginPageResource(
                idpStubsRepository,
                nonSuccessAuthnResponseService,
                new SamlMessageRedirectViewFactory(cookieNames),
                idpUserService,
                sessionRepository,
                cookieFactory);
    }

    @Test
    public void shouldBuildNoAuthnContext(){
        when(sessionRepository.deleteAndGet(SESSION_ID)).thenReturn(Optional.ofNullable(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        when(idaAuthnRequestFromHub.getId()).thenReturn(SAML_REQUEST_ID);
        when(nonSuccessAuthnResponseService.generateNoAuthnContext(anyString(), anyString(), eq(RELAY_STATE))).thenReturn(new SamlResponseFromValue<String>("saml", Function.identity(), RELAY_STATE, URI.create("uri")));

        resource.postNoAuthnContext(IDP_NAME, SESSION_ID);

        verify(nonSuccessAuthnResponseService).generateNoAuthnContext(IDP_NAME, SAML_REQUEST_ID, RELAY_STATE);
    }

    @Test
    public void shouldBuildUpliftFailed() {
        when(sessionRepository.deleteAndGet(SESSION_ID)).thenReturn(Optional.ofNullable(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        when(idaAuthnRequestFromHub.getId()).thenReturn(SAML_REQUEST_ID);
        when(nonSuccessAuthnResponseService.generateUpliftFailed(anyString(), anyString(), eq(RELAY_STATE))).thenReturn(new SamlResponseFromValue<String>("saml", Function.identity(), RELAY_STATE, URI.create("uri")));

        resource.postUpliftFailed(IDP_NAME, SESSION_ID);

        verify(nonSuccessAuthnResponseService).generateUpliftFailed(IDP_NAME, SAML_REQUEST_ID, RELAY_STATE);
    }

    @Test
    public void shouldBuildNoAuthnCancel() {
        when(sessionRepository.deleteAndGet(SESSION_ID)).thenReturn(Optional.ofNullable(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        when(idaAuthnRequestFromHub.getId()).thenReturn(SAML_REQUEST_ID);
        when(nonSuccessAuthnResponseService.generateAuthnCancel(anyString(), anyString(), eq(RELAY_STATE))).thenReturn(new SamlResponseFromValue<String>("saml", Function.identity(), RELAY_STATE, URI.create("uri")));

        resource.post(IDP_NAME, USERNAME, PASSWORD, SubmitButtonValue.Cancel, SESSION_ID);

        verify(nonSuccessAuthnResponseService).generateAuthnCancel(IDP_NAME, SAML_REQUEST_ID, RELAY_STATE);
    }

    @Test
    public void shouldBuildSuccessResponse() throws InvalidUsernameOrPasswordException, InvalidSessionIdException {
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.ofNullable(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        final Response response = resource.post(IDP_NAME, USERNAME, PASSWORD, SubmitButtonValue.SignIn, SESSION_ID);

        verify(idpUserService).attachIdpUserToSession(IDP_NAME, USERNAME, PASSWORD, SESSION_ID);
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
    }

    @Test
    public void shouldBuildAuthnPending(){
        when(sessionRepository.deleteAndGet(SESSION_ID)).thenReturn(Optional.ofNullable(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        when(idaAuthnRequestFromHub.getId()).thenReturn(SAML_REQUEST_ID);
        when(nonSuccessAuthnResponseService.generateAuthnPending(anyString(), anyString(), eq(RELAY_STATE))).thenReturn(new SamlResponseFromValue<String>("saml", Function.identity(), RELAY_STATE, URI.create("uri")));

        resource.postAuthnPending(IDP_NAME, SESSION_ID);

        verify(nonSuccessAuthnResponseService).generateAuthnPending(IDP_NAME, SAML_REQUEST_ID, RELAY_STATE);
    }

    @Test
    public void shouldRedirectToConsentWhenNewlyRegisteredUserReturnsFromHub() {
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.ofNullable(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        Optional<IdpSession> idpSession = Optional.of(Mockito.mock(IdpSession.class));
        when(idpStubsRepository.getIdpWithFriendlyId(IDP_NAME)).thenReturn(idp);
        when(sessionRepository.get(SESSION_ID)).thenReturn(idpSession);
        when(idpSession.get().getIdaAuthnRequestFromHub()).thenReturn(Mockito.mock(IdaAuthnRequestFromHub.class));
        when(idpSession.get().getIdpUser()).thenReturn(Optional.of(databaseIdpUser));
        final Response response = resource.get(IDP_NAME, Optional.of(ErrorMessageType.NO_ERROR), SESSION_ID);
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getLocation().toString()).contains("consent");
    }

    @Test
    public void shouldRedirectLoggedInUserToHomePageIfNoIdaAuthReqFromHub() {
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.ofNullable(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        Optional<IdpSession> idpSession = Optional.of(Mockito.mock(IdpSession.class));
        when(idpStubsRepository.getIdpWithFriendlyId(IDP_NAME)).thenReturn(idp);
        when(sessionRepository.get(SESSION_ID)).thenReturn(idpSession);
        when(idpSession.get().getIdaAuthnRequestFromHub()).thenReturn(null);
        when(idpSession.get().getIdpUser()).thenReturn(Optional.of(databaseIdpUser));
        final Response response = resource.get(IDP_NAME, Optional.of(ErrorMessageType.NO_ERROR), SESSION_ID);
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getLocation().toString()).contains("an%20idp%20name");
    }

    @Test
    public void shouldShowLoginFormWhenNoCookiePresent() {
        when(idpStubsRepository.getIdpWithFriendlyId(IDP_NAME)).thenReturn(idp);
        final Response response = resource.get(IDP_NAME, Optional.of(ErrorMessageType.NO_ERROR), null);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldPresentLoginScreenInWhenThereIsNoActivePreRegSession() {
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.ofNullable(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        Optional<IdpSession> preRegSession = Optional.of(Mockito.mock(IdpSession.class));
        when(sessionRepository.get(SESSION_ID)).thenReturn(preRegSession);
        when(preRegSession.get().getIdpUser()).thenReturn(Optional.empty());
        when(idpStubsRepository.getIdpWithFriendlyId(IDP_NAME)).thenReturn(idp);
        when(idp.getDisplayName()).thenReturn("mock idp display name");
        when(idp.getFriendlyId()).thenReturn("mock idp friendly id");
        when(idp.getAssetId()).thenReturn("mock idp asset id");
        final Response response = resource.get(IDP_NAME, Optional.of(ErrorMessageType.NO_ERROR),SESSION_ID);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldLogUserInAndTakeToHomePageWhenNoIdaReq() {
        final Response response = resource.post(IDP_NAME,USERNAME,PASSWORD, SubmitButtonValue.SignIn, SESSION_ID);
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getLocation().toString()).contains("an%20idp%20name");
    }

    @Test
    public void shouldLogUserInAndTakeToConsentPageWhenIdaReqPresent() {
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.ofNullable(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        final Response response = resource.post(IDP_NAME,USERNAME,PASSWORD, SubmitButtonValue.SignIn, SESSION_ID);
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getLocation().toString()).contains("consent");
    }
}
