package stubsp.stubsp.filters;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.MDC;
import stubidp.stubidp.cookies.HmacValidator;
import stubidp.stubidp.domain.SamlRequest;
import stubidp.stubidp.views.SamlMessageRedirectViewFactory;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static stubidp.stubidp.csrf.AbstractCSRFCheckProtectionFilter.IS_SECURE_COOKIE_ENABLED;
import static stubsp.stubsp.cookies.StubSpCookieNames.SECURE_COOKIE_NAME;
import static stubsp.stubsp.cookies.StubSpCookieNames.SESSION_COOKIE_NAME;

public class RequireValidLoginFilter implements ContainerRequestFilter {

    private final HmacValidator hmacValidator;
    private final boolean isSecureCookieEnabled;
    private final SamlMessageRedirectViewFactory samlMessageRedirectViewFactory;

    public enum Status {VERIFIED, ID_NOT_PRESENT, HASH_NOT_PRESENT, DELETED_SESSION, INVALID_HASH, NOT_FOUND };
    public static final String NO_CURRENT_SESSION_COOKIE_VALUE = "no-current-session";

    @Inject
    public RequireValidLoginFilter(HmacValidator hmacValidator,
                                   @Named(IS_SECURE_COOKIE_ENABLED) Boolean isSecureCookieEnabled,
                                   SamlMessageRedirectViewFactory samlMessageRedirectViewFactory) {
        this.hmacValidator = hmacValidator;
        this.isSecureCookieEnabled = isSecureCookieEnabled;
        this.samlMessageRedirectViewFactory = samlMessageRedirectViewFactory;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {

        // Get SessionId from cookie
        final Optional<String> sessionCookie = Optional.ofNullable(getValueOfPossiblyNullCookie(requestContext.getCookies(), SESSION_COOKIE_NAME));
        // Get SessionId HMAC from cookie
        final Optional<String> secureCookie;
        if (isSecureCookieEnabled) {
            secureCookie = Optional.ofNullable(getValueOfPossiblyNullCookie(requestContext.getCookies(), SECURE_COOKIE_NAME));
        } else {
            secureCookie = Optional.empty();
        }

        if (sessionCookie.isPresent()) {
            MDC.remove("SessionId");
            MDC.put("SessionId", sessionCookie.get());
            if(validateSessionCookies(sessionCookie, secureCookie)) {
                return;
            }
        }

        throw new WebApplicationException(samlMessageRedirectViewFactory.sendSamlRequest(new SamlRequest() {
            @Override
            public String getRequestString() {
                return "a_request";
            }

            @Override
            public String getRelayState() {
                return "a_relaystate";
            }

            @Override
            public URI getIdpSSOUrl() {
                return UriBuilder.fromUri("http://localhost:4000/sso").build();
            }
        }));

    }

    private boolean validateSessionCookies(Optional<String> sessionCookie, Optional<String> secureCookie) {

        final Status status;

        if (sessionCookie.isEmpty() || StringUtils.isEmpty(sessionCookie.get())) {
            status = Status.ID_NOT_PRESENT;
        } else if (isSecureCookieEnabled && (secureCookie.isEmpty() || StringUtils.isEmpty(secureCookie.get()))) {
            status = Status.HASH_NOT_PRESENT;
        } else if (isSecureCookieEnabled && NO_CURRENT_SESSION_COOKIE_VALUE.equals(secureCookie.get())) {
            status = Status.DELETED_SESSION;
        } else if (isSecureCookieEnabled && !hmacValidator.validateHMACSHA256(secureCookie.get(), sessionCookie.get())) {
            status = Status.INVALID_HASH;
        } else if (!sessionExists(new SessionId(sessionCookie.get()))) {
            status = Status.NOT_FOUND;
        } else {
            status = Status.VERIFIED;
        }

        switch (status) {
            case VERIFIED:
                return true;
            case ID_NOT_PRESENT:
            case HASH_NOT_PRESENT:
            case DELETED_SESSION:
            case INVALID_HASH:
            case NOT_FOUND:
            default:
                return false;
        }
    }

    private boolean sessionExists(SessionId sessionId) {
        return false;
    }

    private String getValueOfPossiblyNullCookie(Map<String, Cookie> cookies, String cookieName) {
        return cookies.containsKey(cookieName) ? cookies.get(cookieName).getValue() : null;
    }
}
