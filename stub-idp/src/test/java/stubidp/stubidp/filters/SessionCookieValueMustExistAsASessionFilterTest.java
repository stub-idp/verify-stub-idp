package stubidp.stubidp.filters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.shared.cookies.HmacValidator;
import stubidp.shared.exceptions.InvalidSecureCookieException;
import stubidp.shared.exceptions.SecureCookieNotFoundException;
import stubidp.shared.exceptions.SessionIdCookieNotFoundException;
import stubidp.shared.exceptions.SessionNotFoundException;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.utils.rest.common.SessionId;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static stubidp.stubidp.cookies.StubIdpCookieNames.SECURE_COOKIE_NAME;
import static stubidp.stubidp.cookies.StubIdpCookieNames.SESSION_COOKIE_NAME;
import static stubidp.stubidp.filters.SessionCookieValueMustExistAsASessionFilter.NO_CURRENT_SESSION_COOKIE_VALUE;

@ExtendWith(MockitoExtension.class)
class SessionCookieValueMustExistAsASessionFilterTest {

    private final boolean isSecureCookieEnabled = true;
    @Mock
    private HmacValidator hmacValidator;
    @Mock
    private IdpSessionRepository idpSessionRepository;
    @Mock
    private EidasSessionRepository eidasSessionRepository;
    @Mock
    private ContainerRequestContext containerRequestContext;

    @Test
    public void shouldReturnNullWhenCheckingNotRequiredButNoCookies() {
        Map<String, Cookie> cookies = Map.of();
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        Assertions.assertThrows(SessionIdCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldReturnNullWhenCheckingNotRequiredButSecureCookie() {
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "some-session-id"));
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        Assertions.assertThrows(SecureCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldReturnNullWhenCheckingNotRequiredButSessionCookieIsSetToNoCurrentValue() {
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "some-session-id"), SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, NO_CURRENT_SESSION_COOKIE_VALUE));
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        Assertions.assertThrows(InvalidSecureCookieException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldReturnNullWhenCheckingNotRequiredButSessionCookieAndSecureCookieDontMatchUp() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()), SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie"));
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(false);
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        Assertions.assertThrows(InvalidSecureCookieException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldReturnSessionIdWhenCheckingNotRequiredButSessionCookieAndSecureCookieMatchUp() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()), SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie"));
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        when(idpSessionRepository.containsSession(sessionId)).thenReturn(true);
        new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext);
    }

    @Test
    public void shouldThrowCookieNotFoundExceptionWhenCheckingRequiredButNoCookies() {
        Map<String, Cookie> cookies = Map.of();
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        final SessionIdCookieNotFoundException e = Assertions.assertThrows(SessionIdCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
        assertThat(e.getMessage()).isEqualTo("Unable to locate session from session cookie");
    }

    @Test
    public void shouldThrowSecureCookieNotFoundExceptionWhenCheckingRequiredButNoSessionIdCookie() {
        Map<String, Cookie> cookies = Map.of();
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        final SessionIdCookieNotFoundException e = Assertions.assertThrows(SessionIdCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
        assertThat(e.getMessage()).isEqualTo("Unable to locate session from session cookie");
    }

    @Test
    public void shouldThrowSecureCookieNotFoundExceptionWhenCheckingRequiredButNoSecureCookie() {
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "some-session-id")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        final SecureCookieNotFoundException e = Assertions.assertThrows(SecureCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
        assertThat(e.getMessage()).isEqualTo("Secure cookie not found.");
    }

    @Test
    public void shouldThrowInvalidSecureExceptionWhenCheckingRequiredButSessionCookieIsSetToNoCurrentValue() {
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "session-id"),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, NO_CURRENT_SESSION_COOKIE_VALUE)
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        final InvalidSecureCookieException e = Assertions.assertThrows(InvalidSecureCookieException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
        assertThat(e.getMessage()).isEqualTo("Secure cookie was set to deleted session value, indicating a previously completed session.");
    }

    @Test
    public void shoulThrowInvalidSecureCookieExceptionWhenCheckingRequiredButSessionCookieAndSecureCookieDontMatchUp() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(false);
        final InvalidSecureCookieException e = Assertions.assertThrows(InvalidSecureCookieException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
        assertThat(e.getMessage()).isEqualTo("Secure cookie value not valid.");
    }

    @Test
    public void shouldThrowNotFoundIfSessionNotActive() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        Assertions.assertThrows(SessionNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldIgnoreSecureCookieIfSecureCookiesNotEnabled() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookies")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        when(idpSessionRepository.containsSession(sessionId)).thenReturn(true);
        new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, false).filter(containerRequestContext);
    }
}
