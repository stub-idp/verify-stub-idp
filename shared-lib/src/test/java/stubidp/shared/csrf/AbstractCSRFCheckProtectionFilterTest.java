package stubidp.shared.csrf;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.shared.cookies.CookieNames;
import stubidp.shared.cookies.HmacValidator;
import stubidp.shared.csrf.exceptions.CSRFBodyNotFoundException;
import stubidp.shared.csrf.exceptions.CSRFCouldNotValidateSessionException;
import stubidp.shared.csrf.exceptions.CSRFNoTokenInSessionException;
import stubidp.shared.csrf.exceptions.CSRFTokenNotFoundException;
import stubidp.shared.csrf.exceptions.CSRFTokenWasInvalidException;
import stubidp.utils.rest.common.SessionId;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractCSRFCheckProtectionFilterTest {

    private boolean isSecureCookieEnabled = true;
    private final CookieNames cookieNames = new CookieNames() {
        @Override
        public String getSessionCookieName() {
            return "sessionCookie";
        }

        @Override
        public String getSecureCookieName() {
            return "secureCookie";
        }
    };

    @Mock
    private HmacValidator hmacValidator;
    @Mock
    private ContainerRequestContext containerRequestContext;

    private SessionId sessionId;

    private static boolean sessionExists = true;
    private static Optional<String> tokenFromSession = Optional.empty();

    private AbstractCSRFCheckProtectionFilter abstractCSRFCheckProtectionFilter;

    @BeforeEach
    void setUp() {
        sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(
                cookieNames.getSessionCookieName(), new NewCookie(cookieNames.getSessionCookieName(), sessionId.toString()),
                cookieNames.getSecureCookieName(), new NewCookie(cookieNames.getSecureCookieName(), "secure-cookie")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        sessionExists = true;
        tokenFromSession = Optional.of("foo");
        abstractCSRFCheckProtectionFilter = new AbstractCSRFCheckProtectionFilter(hmacValidator, isSecureCookieEnabled, cookieNames) {
            @Override
            protected Optional<String> getTokenFromSession(SessionId sessionId) { return tokenFromSession; }
            @Override
            protected boolean sessionExists(SessionId sessionId) { return sessionExists; }
        };
    }

    @Test
    void shouldValidateSession() throws Exception {
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(false);
        Assertions.assertThrows(CSRFCouldNotValidateSessionException.class, () -> abstractCSRFCheckProtectionFilter.filter(containerRequestContext));
    }

    @Test
    void shouldValidateEntityExists() throws Exception {
        when(containerRequestContext.hasEntity()).thenReturn(false);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        Assertions.assertThrows(CSRFBodyNotFoundException.class, () -> abstractCSRFCheckProtectionFilter.filter(containerRequestContext));
    }

    @Test
    void shouldCheckTokenExistsInTheSession() throws Exception {
        final String csrfToken = "foo";
        final String entity = "a=1&b=2&c=3&"+AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY+"="+csrfToken;
        tokenFromSession = Optional.empty();
        when(containerRequestContext.hasEntity()).thenReturn(true);
        when(containerRequestContext.getEntityStream()).thenReturn(new ByteArrayInputStream(entity.getBytes()));
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        Assertions.assertThrows(CSRFNoTokenInSessionException.class, () -> abstractCSRFCheckProtectionFilter.filter(containerRequestContext));
    }

    @Test
    void shouldCheckTokenWithTheOneInTheSession() throws Exception {
        final String entity = "a=1&b=2&c=3&"+AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY+"=not_this_token";
        when(containerRequestContext.hasEntity()).thenReturn(true);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        when(containerRequestContext.getEntityStream()).thenReturn(new ByteArrayInputStream(entity.getBytes()));
        Assertions.assertThrows(CSRFTokenWasInvalidException.class, () -> abstractCSRFCheckProtectionFilter.filter(containerRequestContext));
    }

    @Test
    void shouldErrorIfTokenNotFound() throws Exception {
        final String entity = "a=1&b=2&c=3&";
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        when(containerRequestContext.hasEntity()).thenReturn(true);
        when(containerRequestContext.getEntityStream()).thenReturn(new ByteArrayInputStream(entity.getBytes()));
        Assertions.assertThrows(CSRFTokenNotFoundException.class, () -> abstractCSRFCheckProtectionFilter.filter(containerRequestContext));
    }

    @Test
    void shouldAddCSRFProtectionToAllForms() throws Exception {
        final String csrfToken = "foo";
        final String entity = "a=1&b=2&c=3&"+AbstractCSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY+"="+csrfToken;
        tokenFromSession = Optional.of(csrfToken);
        when(containerRequestContext.hasEntity()).thenReturn(true);
        when(containerRequestContext.getEntityStream()).thenReturn(new ByteArrayInputStream(entity.getBytes()));
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        abstractCSRFCheckProtectionFilter.filter(containerRequestContext);
        ArgumentCaptor<InputStream> argumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(containerRequestContext, times(1)).setEntityStream(argumentCaptor.capture());
        assertThat(IOUtils.toByteArray(argumentCaptor.getValue())).isEqualTo(entity.getBytes());
    }
}
