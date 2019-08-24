package stubidp.stubidp.csrf;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.stubidp.cookies.HmacValidator;
import stubidp.stubidp.csrf.exceptions.CSRFBodyNotFoundException;
import stubidp.stubidp.csrf.exceptions.CSRFCouldNotValidateSessionException;
import stubidp.stubidp.csrf.exceptions.CSRFNoTokenInSessionException;
import stubidp.stubidp.csrf.exceptions.CSRFTokenNotFoundException;
import stubidp.stubidp.csrf.exceptions.CSRFTokenWasInvalidException;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
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
import static stubidp.stubidp.cookies.CookieNames.SECURE_COOKIE_NAME;
import static stubidp.stubidp.cookies.CookieNames.SESSION_COOKIE_NAME;
import static stubidp.stubidp.csrf.CSRFCheckProtectionFilter.CSRF_PROTECT_FORM_KEY;

@ExtendWith(MockitoExtension.class)
public class CSRFCheckProtectionFilterTest {

    private boolean isSecureCookieEnabled = true;
    @Mock
    private HmacValidator hmacValidator;
    @Mock
    private IdpSessionRepository idpSessionRepository;
    @Mock
    private EidasSessionRepository eidasSessionRepository;
    @Mock
    private ContainerRequestContext containerRequestContext;
    @Mock
    private IdpSession session;

    private SessionId sessionId;

    @BeforeEach
    public void setUp() {
        sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
    }

    @Test
    public void shouldValidateSession() throws Exception {
        final String csrfToken = "foo";

        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(false);

        Assertions.assertThrows(CSRFCouldNotValidateSessionException.class, () -> new CSRFCheckProtectionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldValidateEntityExists() throws Exception {
        when(idpSessionRepository.containsSession(sessionId)).thenReturn(true);
        when(containerRequestContext.hasEntity()).thenReturn(true);

        final String csrfToken = "foo";

        when(containerRequestContext.hasEntity()).thenReturn(false);

        Assertions.assertThrows(CSRFBodyNotFoundException.class, () -> new CSRFCheckProtectionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldCheckTokenExistsInTheSession() throws Exception {
        when(idpSessionRepository.containsSession(sessionId)).thenReturn(true);
        when(idpSessionRepository.get(sessionId)).thenReturn(Optional.ofNullable(session));
        when(containerRequestContext.hasEntity()).thenReturn(true);

        final String csrfToken = "foo";
        final String entity = "a=1&b=2&c=3&"+CSRF_PROTECT_FORM_KEY+"="+csrfToken;
        when(containerRequestContext.getEntityStream()).thenReturn(new ByteArrayInputStream(entity.getBytes()));
        when(session.getCsrfToken()).thenReturn(null);

        when(containerRequestContext.hasEntity()).thenReturn(true);

        Assertions.assertThrows(CSRFNoTokenInSessionException.class, () -> new CSRFCheckProtectionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldCheckTokenWithTheOneInTheSession() throws Exception {
        when(idpSessionRepository.containsSession(sessionId)).thenReturn(true);
        when(idpSessionRepository.get(sessionId)).thenReturn(Optional.ofNullable(session));
        when(containerRequestContext.hasEntity()).thenReturn(true);

        final String csrfToken = "foo";
        final String entity = "a=1&b=2&c=3&"+CSRF_PROTECT_FORM_KEY+"=not_this_token";
        when(containerRequestContext.getEntityStream()).thenReturn(new ByteArrayInputStream(entity.getBytes()));
        when(session.getCsrfToken()).thenReturn(csrfToken);

        Assertions.assertThrows(CSRFTokenWasInvalidException.class, () -> new CSRFCheckProtectionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldErrorIfTokenNotFound() throws Exception {
        when(idpSessionRepository.containsSession(sessionId)).thenReturn(true);
        when(containerRequestContext.hasEntity()).thenReturn(true);

        final String entity = "a=1&b=2&c=3&";
        when(containerRequestContext.getEntityStream()).thenReturn(new ByteArrayInputStream(entity.getBytes()));

        Assertions.assertThrows(CSRFTokenNotFoundException.class, () -> new CSRFCheckProtectionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext));
    }

    @Test
    public void shouldAddCSRFProtectionToAllForms() throws Exception {
        when(idpSessionRepository.containsSession(sessionId)).thenReturn(true);
        when(idpSessionRepository.get(sessionId)).thenReturn(Optional.ofNullable(session));
        when(containerRequestContext.hasEntity()).thenReturn(true);

        final String csrfToken = "foo";
        final String entity = "a=1&b=2&c=3&"+CSRF_PROTECT_FORM_KEY+"="+csrfToken;
        when(containerRequestContext.getEntityStream()).thenReturn(new ByteArrayInputStream(entity.getBytes()));
        when(session.getCsrfToken()).thenReturn(csrfToken);

        new CSRFCheckProtectionFilter(idpSessionRepository, eidasSessionRepository, hmacValidator, isSecureCookieEnabled).filter(containerRequestContext);

        ArgumentCaptor<InputStream> argumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(containerRequestContext, times(1)).setEntityStream(argumentCaptor.capture());
        assertThat(IOUtils.toByteArray(argumentCaptor.getValue())).isEqualTo(entity.getBytes());
    }


}
