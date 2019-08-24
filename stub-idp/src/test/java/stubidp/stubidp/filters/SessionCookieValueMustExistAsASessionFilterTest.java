package stubidp.stubidp.filters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.stubidp.cookies.HmacValidator;
import stubidp.stubidp.exceptions.InvalidSecureCookieException;
import stubidp.stubidp.exceptions.SecureCookieNotFoundException;
import stubidp.stubidp.exceptions.SessionIdCookieNotFoundException;
import stubidp.stubidp.exceptions.SessionNotFoundException;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.utils.rest.common.SessionId;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static stubidp.stubidp.cookies.CookieNames.SECURE_COOKIE_NAME;
import static stubidp.stubidp.cookies.CookieNames.SESSION_COOKIE_NAME;
import static stubidp.stubidp.filters.SessionCookieValueMustExistAsASessionFilter.NO_CURRENT_SESSION_COOKIE_VALUE;

@ExtendWith(MockitoExtension.class)
public class SessionCookieValueMustExistAsASessionFilterTest {

    private boolean isSecureCookieEnabled = true;
    @Mock
    private HmacValidator hmacValidator;
    @Mock
    private IdpSessionRepository idpSessionRepository;
    @Mock
    private EidasSessionRepository eidasSessionRepository;
    @Mock
    private ContainerRequestContext containerRequestContext;

    @Test
    public void shouldReturnNullWhenCheckingNotRequiredButNoCookies() throws Exception {
        Map<String, Cookie> cookies = Map.of();
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        Assertions.assertThrows(SessionIdCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldReturnNullWhenCheckingNotRequiredButSecureCookie() throws Exception {
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "some-session-id"));
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        Assertions.assertThrows(SecureCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldReturnNullWhenCheckingNotRequiredButSessionCookieIsSetToNoCurrentValue() throws Exception {
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "some-session-id"), SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, NO_CURRENT_SESSION_COOKIE_VALUE));
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        Assertions.assertThrows(InvalidSecureCookieException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldReturnNullWhenCheckingNotRequiredButSessionCookieAndSecureCookieDontMatchUp() throws Exception {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()), SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie"));
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(false);
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        Assertions.assertThrows(InvalidSecureCookieException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldReturnSessionIdWhenCheckingNotRequiredButSessionCookieAndSecureCookieMatchUp() throws Exception {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()), SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie"));
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        when(idpSessionRepository.containsSession(sessionId)).thenReturn(true);
        new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext);
    }

    @Test
    public void shouldThrowCookieNotFoundExceptionWhenCheckingRequiredButNoCookies() throws Exception {
        Map<String, Cookie> cookies = Map.of();
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        final SessionIdCookieNotFoundException e = Assertions.assertThrows(SessionIdCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
        assertThat(e.getMessage()).isEqualTo("Unable to locate session from session cookie");
    }

    @Test
    public void shouldThrowSecureCookieNotFoundExceptionWhenCheckingRequiredButNoSessionIdCookie() throws Exception {
        Map<String, Cookie> cookies = Map.of();
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        final SessionIdCookieNotFoundException e = Assertions.assertThrows(SessionIdCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
        assertThat(e.getMessage()).isEqualTo("Unable to locate session from session cookie");
    }

    @Test
    public void shouldThrowSecureCookieNotFoundExceptionWhenCheckingRequiredButNoSecureCookie() throws Exception {
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "some-session-id")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        final SecureCookieNotFoundException e = Assertions.assertThrows(SecureCookieNotFoundException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
        assertThat(e.getMessage()).isEqualTo("Secure cookie not found.");
    }

    @Test
    public void shouldThrowInvalidSecureExceptionWhenCheckingRequiredButSessionCookieIsSetToNoCurrentValue() throws Exception {
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "session-id"),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, NO_CURRENT_SESSION_COOKIE_VALUE)
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        final InvalidSecureCookieException e = Assertions.assertThrows(InvalidSecureCookieException.class, () -> new SessionCookieValueMustExistAsASessionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
        assertThat(e.getMessage()).isEqualTo("Secure cookie was set to deleted session value, indicating a previously completed session.");
    }

    @Test
    public void shoulThrowInvalidSecureCookieExceptionWhenCheckingRequiredButSessionCookieAndSecureCookieDontMatchUp() throws Exception {
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
    public void shouldThrowNotFoundIfSessionNotActive() throws Exception {
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
    public void shouldIgnoreSecureCookieIfSecureCookiesNotEnabled() throws Exception {
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
