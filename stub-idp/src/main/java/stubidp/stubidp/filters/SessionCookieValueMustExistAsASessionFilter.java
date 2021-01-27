package stubidp.stubidp.filters;

import org.jboss.logging.MDC;
import stubidp.shared.cookies.CookieNames;
import stubidp.shared.cookies.HmacValidator;
import stubidp.shared.exceptions.InvalidSecureCookieException;
import stubidp.shared.exceptions.SecureCookieNotFoundException;
import stubidp.shared.exceptions.SessionIdCookieNotFoundException;
import stubidp.shared.exceptions.SessionNotFoundException;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import java.util.Map;
import java.util.Optional;

import static stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter.IS_SECURE_COOKIE_ENABLED;

public class SessionCookieValueMustExistAsASessionFilter implements ContainerRequestFilter {

    private final IdpSessionRepository idpSessionRepository;
    private final EidasSessionRepository eidasSessionRepository;
    private final HmacValidator hmacValidator;
    private final boolean isSecureCookieEnabled;
    private final CookieNames cookieNames = new StubIdpCookieNames();

    public enum Status {VERIFIED, ID_NOT_PRESENT, HASH_NOT_PRESENT, DELETED_SESSION, INVALID_HASH, NOT_FOUND }

    public static final String NO_CURRENT_SESSION_COOKIE_VALUE = "no-current-session";

    @Inject
    public SessionCookieValueMustExistAsASessionFilter(IdpSessionRepository idpSessionRepository,
                                                       EidasSessionRepository eidasSessionRepository,
                                                       HmacValidator hmacValidator,
                                                       @Named(IS_SECURE_COOKIE_ENABLED) Boolean isSecureCookieEnabled) {
        this.idpSessionRepository = idpSessionRepository;
        this.eidasSessionRepository = eidasSessionRepository;
        this.hmacValidator = hmacValidator;
        this.isSecureCookieEnabled = isSecureCookieEnabled;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {

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

        validateSessionCookies(sessionCookie, secureCookie);
    }

    public void validateSessionCookies(Optional<String> sessionCookie, Optional<String> secureCookie) {

        final Status status;

        if (sessionCookie.get().isBlank()) {
            status = Status.ID_NOT_PRESENT;
        } else if (isSecureCookieEnabled && (secureCookie.isEmpty() || secureCookie.get().isBlank())) {
            status = Status.HASH_NOT_PRESENT;
        } else if (isSecureCookieEnabled && NO_CURRENT_SESSION_COOKIE_VALUE.equals(secureCookie.get())) {
            status = Status.DELETED_SESSION;
        } else if (isSecureCookieEnabled && !hmacValidator.validateHMACSHA256(secureCookie.get(), sessionCookie.get())) {
            status = Status.INVALID_HASH;
        } else if (!idpSessionRepository.containsSession(new SessionId(sessionCookie.get())) && !eidasSessionRepository.containsSession(new SessionId(sessionCookie.get()))) {
            status = Status.NOT_FOUND;
        } else {
            status = Status.VERIFIED;
        }

        switch (status) {
            case VERIFIED:
                break;
            case ID_NOT_PRESENT:
                throw new SessionIdCookieNotFoundException("Session id was Null or Empty, cannot match to the secure cookie.");
            case HASH_NOT_PRESENT:
                throw new SecureCookieNotFoundException("Secure cookie not found.", new SessionId(sessionCookie.get()));
            case DELETED_SESSION:
                throw new InvalidSecureCookieException("Secure cookie was set to deleted session value, indicating a previously completed session.", new SessionId(sessionCookie.get()));
            case INVALID_HASH:
                throw new InvalidSecureCookieException("Secure cookie value not valid.", new SessionId(sessionCookie.get()));
            case NOT_FOUND:
                throw new SessionNotFoundException("Session not found - restart journey.", new SessionId(sessionCookie.get()));
            default:
                throw new IllegalStateException("Impossible to get here");
        }
    }

    private String getValueOfPossiblyNullCookie(Map<String, Cookie> cookies, String cookieName) {
        return cookies.containsKey(cookieName) ? cookies.get(cookieName).getValue() : null;
    }
}
