package stubsp.stubsp.filters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.shared.cookies.HmacValidator;
import stubidp.shared.repositories.MetadataRepository;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.utils.rest.common.SessionId;
import stubsp.stubsp.configuration.StubSpConfiguration;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static stubsp.stubsp.cookies.StubSpCookieNames.SECURE_COOKIE_NAME;
import static stubsp.stubsp.cookies.StubSpCookieNames.SESSION_COOKIE_NAME;
import static stubsp.stubsp.filters.RequireValidLoginFilter.NO_CURRENT_SESSION_COOKIE_VALUE;

@ExtendWith(MockitoExtension.class)
class RequireValidLoginFilterTest {

    private final boolean isSecureCookieEnabled = true;
    @Mock
    private HmacValidator hmacValidator;
    @Mock
    private ContainerRequestContext containerRequestContext;
    @Mock
    private SamlMessageRedirectViewFactory samlMessageRedirectViewFactory;
    @Mock
    private MetadataRepository metadataRepository;
    @Mock
    private StubSpConfiguration stubSpConfiguration;

    @Test
    void shouldReturnNullWhenCheckingNotRequiredButNoCookies() {
        Map<String, Cookie> cookies = Map.of();
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shouldReturnNullWhenCheckingNotRequiredButSecureCookie() {
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "some-session-id"));
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shouldReturnNullWhenCheckingNotRequiredButSessionCookieIsSetToNoCurrentValue() {
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "some-session-id"), SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, NO_CURRENT_SESSION_COOKIE_VALUE));
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shouldReturnNullWhenCheckingNotRequiredButSessionCookieAndSecureCookieDontMatchUp() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()), SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie"));
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(false);
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shouldReturnSessionIdWhenCheckingNotRequiredButSessionCookieAndSecureCookieMatchUp() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()), SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie"));
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration).filter(containerRequestContext);
    }

    @Test
    void shouldThrowCookieNotFoundExceptionWhenCheckingRequiredButNoCookies() {
        Map<String, Cookie> cookies = Map.of();
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shouldThrowSecureCookieNotFoundExceptionWhenCheckingRequiredButNoSessionIdCookie() {
        Map<String, Cookie> cookies = Map.of();
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shouldThrowSecureCookieNotFoundExceptionWhenCheckingRequiredButNoSecureCookie() {
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "some-session-id")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shouldThrowInvalidSecureExceptionWhenCheckingRequiredButSessionCookieIsSetToNoCurrentValue() {
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, "session-id"),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, NO_CURRENT_SESSION_COOKIE_VALUE)
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shoulThrowInvalidSecureCookieExceptionWhenCheckingRequiredButSessionCookieAndSecureCookieDontMatchUp() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(false);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shouldThrowNotFoundIfSessionNotActive() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookie")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        when(hmacValidator.validateHMACSHA256("secure-cookie", sessionId.getSessionId())).thenReturn(true);
        validateThrowsAndPostsSamlToIdp(new RequireValidLoginFilter(hmacValidator, isSecureCookieEnabled, samlMessageRedirectViewFactory, false, metadataRepository, stubSpConfiguration), containerRequestContext);
    }

    @Test
    void shouldIgnoreSecureCookieIfSecureCookiesNotEnabled() {
        SessionId sessionId = SessionId.createNewSessionId();
        Map<String, Cookie> cookies = Map.of(
                SESSION_COOKIE_NAME, new NewCookie(SESSION_COOKIE_NAME, sessionId.toString()),
                SECURE_COOKIE_NAME, new NewCookie(SECURE_COOKIE_NAME, "secure-cookies")
        );
        when(containerRequestContext.getCookies()).thenReturn(cookies);
        new RequireValidLoginFilter(hmacValidator, false, samlMessageRedirectViewFactory, true, metadataRepository, stubSpConfiguration).filter(containerRequestContext);
    }

    private void validateThrowsAndPostsSamlToIdp(RequireValidLoginFilter requireValidLoginFilter, ContainerRequestContext containerRequestContext) {
        when(samlMessageRedirectViewFactory.sendSamlRequest(any())).thenReturn(Response.ok().entity("Saml Processing...").build());
        final WebApplicationException exception = Assertions.assertThrows(WebApplicationException.class, () -> requireValidLoginFilter.filter(containerRequestContext));
        assertThat(exception.getResponse().getStatus()).isEqualTo(200);
        assertThat(exception.getResponse().hasEntity()).isTrue();
        assertThat(exception.getResponse().getEntity().toString()).contains("Saml Processing...");
    }
}
