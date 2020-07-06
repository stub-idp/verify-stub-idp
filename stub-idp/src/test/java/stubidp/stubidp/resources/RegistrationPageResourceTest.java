package stubidp.stubidp.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.shared.cookies.CookieNames;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.domain.SamlResponseFromValue;
import stubidp.stubidp.exceptions.IncompleteRegistrationException;
import stubidp.stubidp.exceptions.InvalidDateException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.exceptions.UsernameAlreadyTakenException;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.resources.idp.RegistrationPageResource;
import stubidp.stubidp.services.IdpUserService;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
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
import static stubidp.saml.domain.assertions.AuthnContext.LEVEL_2;
import static stubidp.stubidp.domain.SubmitButtonValue.Cancel;
import static stubidp.stubidp.domain.SubmitButtonValue.Register;

@ExtendWith(MockitoExtension.class)
public class RegistrationPageResourceTest {

    private final String IDP_NAME = "an idp name";
    private final SessionId SESSION_ID = SessionId.createNewSessionId();
    private final String RELAY_STATE = "relayState";
    private final String SAML_REQUEST_ID = "samlRequestId";

    private RegistrationPageResource resource;

    @Mock
    private IdpStubsRepository idpStubsRepository;
    @Mock
    private IdpUserService idpUserService;
    @Mock
    private NonSuccessAuthnResponseService nonSuccessAuthnResponseService;
    @Mock
    private IdpSessionRepository idpSessionRepository;
    @Mock
    private IdaAuthnRequestFromHub idaAuthnRequestFromHub;
    @Mock
    private IdpSession idpSession;
    @Mock
    private CookieNames cookieNames;

    @BeforeEach
    public void createResource() {
        resource = new RegistrationPageResource(
                idpStubsRepository,
                idpUserService,
                new SamlMessageRedirectViewFactory(cookieNames),
                nonSuccessAuthnResponseService,
                idpSessionRepository
        );
    }

    @Test
    public void shouldHaveStatusAuthnCancelledResponseWhenUserCancels() {
        when(idpSessionRepository.get(SESSION_ID)).thenReturn(Optional.of(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        when(idpSessionRepository.deleteAndGet(SESSION_ID)).thenReturn(Optional.of(new IdpSession(SESSION_ID, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null)));
        when(idaAuthnRequestFromHub.getId()).thenReturn(SAML_REQUEST_ID);
        when(nonSuccessAuthnResponseService.generateAuthnCancel(anyString(), anyString(), eq(RELAY_STATE))).thenReturn(new SamlResponseFromValue<>("saml", Function.identity(), RELAY_STATE, URI.create("uri")));
        when(cookieNames.getSessionCookieName()).thenReturn("sessionCookieName");

        resource.post(IDP_NAME, null, null, null, null, null, null, null, false, null, null, null, null, Cancel, SESSION_ID);

        verify(nonSuccessAuthnResponseService).generateAuthnCancel(IDP_NAME, SAML_REQUEST_ID, RELAY_STATE);
    }

    @Test
    public void shouldHaveResponseStatusRedirectWhenUserRegisters() throws InvalidSessionIdException, IncompleteRegistrationException, InvalidDateException, UsernameAlreadyTakenException, InvalidUsernameOrPasswordException {
        when(idaAuthnRequestFromHub.getId()).thenReturn(SAML_REQUEST_ID);
        when(idpSessionRepository.get(SESSION_ID)).thenReturn(Optional.of(idpSession));
        when(idpSession.getIdaAuthnRequestFromHub()).thenReturn(idaAuthnRequestFromHub);
        final Response response = resource.post(IDP_NAME, "bob", "jones", "address line 1", "address line 2", "address town", "address postcode", "2000-01-01", false, null, "username", "password", LEVEL_2, Register, SESSION_ID);

        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getLocation().toString()).contains("consent");
        verify(idpUserService).createAndAttachIdpUserToSession(eq(IDP_NAME), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), eq(LEVEL_2), anyString(), eq(Optional.empty()), anyString(), anyString(), eq(SESSION_ID));
    }

    @Test
    public void shouldHaveResponseStatusRedirectWhenUserPreRegisters() throws InvalidSessionIdException, IncompleteRegistrationException, InvalidDateException, UsernameAlreadyTakenException, InvalidUsernameOrPasswordException {
        when(idpSessionRepository.get(SESSION_ID)).thenReturn(Optional.of(idpSession));
        when(idpSession.getIdaAuthnRequestFromHub()).thenReturn(null);
        final Response response = resource.post(IDP_NAME, "bob", "jones", "address line 1", "address line 2", "address town", "address postcode", "2000-01-01", false, null, "username", "password", LEVEL_2, Register, SESSION_ID);

        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getLocation().toString()).contains("start-prompt");
        verify(idpUserService).createAndAttachIdpUserToSession(eq(IDP_NAME), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), eq(LEVEL_2), anyString(), eq(Optional.empty()), anyString(), anyString(), eq(SESSION_ID));
    }
}
