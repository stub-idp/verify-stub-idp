package stubidp.stubidp.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.shared.cookies.CookieNames;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.domain.EidasAuthnRequest;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.domain.SamlResponseFromValue;
import stubidp.stubidp.exceptions.IncompleteRegistrationException;
import stubidp.stubidp.exceptions.InvalidDateException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.exceptions.UsernameAlreadyTakenException;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.stubidp.resources.eidas.EidasRegistrationPageResource;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.services.StubCountryService;
import stubidp.stubidp.views.SignAssertions;
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
class EidasRegistrationPageResourceTest {

    private final String STUB_COUNTRY = "stub-country";
    private final SessionId SESSION_ID = SessionId.createNewSessionId();
    private final String RELAY_STATE = "relayState";
    private final String SAML_REQUEST_ID = "samlRequestId";
    private EidasSession eidasSession;

    private EidasRegistrationPageResource resource;

    @Mock
    private StubCountryRepository stubCountryRepository;
    @Mock
    private StubCountryService stubCountryService;
    @Mock
    private NonSuccessAuthnResponseService nonSuccessAuthnResponseService;
    @Mock
    private EidasSessionRepository sessionRepository;
    @Mock
    private EidasAuthnRequest eidasAuthnRequest;
    @Mock
    private CookieNames cookieNames;

    @BeforeEach
    void createResource() {
        resource = new EidasRegistrationPageResource(
                stubCountryRepository,
                stubCountryService,
                new SamlMessageRedirectViewFactory(cookieNames),
                nonSuccessAuthnResponseService,
                sessionRepository
        );

        eidasSession = new EidasSession(SESSION_ID, eidasAuthnRequest, RELAY_STATE, null, null, null, null);
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.ofNullable(eidasSession));
        when(eidasAuthnRequest.getRequestId()).thenReturn(SAML_REQUEST_ID);
    }

    @Test
    void shouldHaveStatusAuthnCancelledResponseWhenUserCancels(){
        when(sessionRepository.deleteAndGet(SESSION_ID)).thenReturn(Optional.of(new EidasSession(SESSION_ID, eidasAuthnRequest, RELAY_STATE, null, null, null, null)));
        when(nonSuccessAuthnResponseService.generateAuthnCancel(anyString(), anyString(), eq(RELAY_STATE))).thenReturn(new SamlResponseFromValue<String>("saml", Function.identity(), RELAY_STATE, URI.create("uri")));
        when(cookieNames.getSessionCookieName()).thenReturn("sessionCookieName");

        resource.post(STUB_COUNTRY, null, null, null, null, null, null, null, null, Cancel, Optional.of(SignAssertions.signAssertions), SESSION_ID);

        verify(nonSuccessAuthnResponseService).generateAuthnCancel(STUB_COUNTRY, SAML_REQUEST_ID, RELAY_STATE);
    }

    @Test
    void shouldHaveStatusSuccessResponseWhenUserRegisters() throws InvalidSessionIdException, IncompleteRegistrationException, InvalidDateException, UsernameAlreadyTakenException, InvalidUsernameOrPasswordException {

        final Response response = resource.post(STUB_COUNTRY, "bob", "", "jones", "", "2000-01-01", "username", "password", LEVEL_2, Register, Optional.of(SignAssertions.signAssertions), SESSION_ID);

        assertThat(response.getStatus()).isEqualTo(303);
        verify(stubCountryService).createAndAttachIdpUserToSession(eq(EidasScheme.fromString(STUB_COUNTRY).get()), anyString(), anyString(), eq(eidasSession), anyString(), anyString(), anyString(), anyString(), anyString(), eq(LEVEL_2));
    }
}
