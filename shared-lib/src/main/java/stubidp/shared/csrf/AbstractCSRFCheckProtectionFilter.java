package stubidp.shared.csrf;

import io.dropwizard.util.Strings;
import org.jboss.logging.MDC;
import stubidp.shared.cookies.CookieNames;
import stubidp.shared.cookies.HmacValidator;
import stubidp.shared.csrf.exceptions.CSRFBodyNotFoundException;
import stubidp.shared.csrf.exceptions.CSRFCouldNotValidateSessionException;
import stubidp.shared.csrf.exceptions.CSRFNoTokenInSessionException;
import stubidp.shared.csrf.exceptions.CSRFTokenNotFoundException;
import stubidp.shared.csrf.exceptions.CSRFTokenWasInvalidException;
import stubidp.shared.exceptions.SessionIdCookieNotFoundException;
import stubidp.utils.rest.common.SessionId;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Use the "Synchronizer Token Pattern" to implement CSRF
 * see https://en.wikipedia.org/wiki/Cross-site_request_forgery
 */
@Priority(Priorities.AUTHENTICATION)
public abstract class AbstractCSRFCheckProtectionFilter implements ContainerRequestFilter {

    public static final String IS_SECURE_COOKIE_ENABLED = "isSecureCookieEnabled";
    public static final String CSRF_PROTECT_FORM_KEY = "csrf_protect";
    private static final String NO_CURRENT_SESSION_COOKIE_VALUE = "no-current-session";

    private final HmacValidator hmacValidator;
    private final boolean isSecureCookieEnabled;
    private final CookieNames cookieNames;

    public enum Status {VERIFIED, ID_NOT_PRESENT, HASH_NOT_PRESENT, DELETED_SESSION, INVALID_HASH, NOT_FOUND }

    public AbstractCSRFCheckProtectionFilter(HmacValidator hmacValidator, boolean isSecureCookieEnabled, CookieNames cookieNames) {
        this.hmacValidator = hmacValidator;
        this.isSecureCookieEnabled = isSecureCookieEnabled;
        this.cookieNames = cookieNames;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        final SessionId sessionId = getValidSessionId(requestContext);

        if(!requestContext.hasEntity()) {
            throw new CSRFBodyNotFoundException();
        }

        // slurp the request entity - treat it as an opaque string
        final String requestBody = new String(requestContext.getEntityStream().readAllBytes(), UTF_8);

        // validate the csrf token
        for(String keyValue: requestBody.split("&")) {
            final String[] split = keyValue.split("=");
            if(split[0].equals(CSRF_PROTECT_FORM_KEY)) {
                final Optional<String> tokenInSession = getTokenFromSession(sessionId);

                if(tokenInSession.isEmpty()) {
                    throw new CSRFNoTokenInSessionException();
                }

                if(!tokenInSession.get().equals(split[1].trim())) {
                    throw new CSRFTokenWasInvalidException();
                }

                requestContext.setEntityStream(new ByteArrayInputStream(requestBody.getBytes()));
                return;
            }
        }

        throw new CSRFTokenNotFoundException();
    }

    protected abstract Optional<String> getTokenFromSession(SessionId sessionId);
    protected abstract boolean sessionExists(SessionId sessionId);

    private SessionId getValidSessionId(ContainerRequestContext requestContext) {

        // Get SessionId from cookie
        final Optional<String> sessionCookie = Optional.ofNullable(getValueOfPossiblyNullCookie(requestContext.getCookies(), cookieNames.getSessionCookieName()));
        // Get SessionId HMAC from cookie
        final Optional<String> secureCookie;
        if (isSecureCookieEnabled) {
            secureCookie = Optional.ofNullable(getValueOfPossiblyNullCookie(requestContext.getCookies(), cookieNames.getSecureCookieName()));
        } else {
            secureCookie = Optional.empty();
        }

        if (sessionCookie.isEmpty()) {
            throw new SessionIdCookieNotFoundException("Unable to locate session from session cookie");
        } else {
            MDC.remove("SessionId");
            MDC.put("SessionId", sessionCookie.get());
        }

        final Status status;

        if (Strings.isNullOrEmpty(sessionCookie.get())) {
            status = Status.ID_NOT_PRESENT;
        } else if (isSecureCookieEnabled && (secureCookie.isEmpty() || Strings.isNullOrEmpty(secureCookie.get()))) {
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

        if(Status.VERIFIED != status) {
            throw new CSRFCouldNotValidateSessionException(status);
        }

        return new SessionId(sessionCookie.get());
    }

    private static String getValueOfPossiblyNullCookie(Map<String, Cookie> cookies, String cookieName) {
        return cookies.containsKey(cookieName) ? cookies.get(cookieName).getValue() : null;
    }

}
