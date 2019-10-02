package stubidp.stubidp.csrf;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.MDC;
import stubidp.stubidp.cookies.HmacValidator;
import stubidp.stubidp.csrf.exceptions.CSRFBodyNotFoundException;
import stubidp.stubidp.csrf.exceptions.CSRFCouldNotValidateSessionException;
import stubidp.stubidp.csrf.exceptions.CSRFNoTokenInSessionException;
import stubidp.stubidp.csrf.exceptions.CSRFTokenNotFoundException;
import stubidp.stubidp.csrf.exceptions.CSRFTokenWasInvalidException;
import stubidp.stubidp.exceptions.SessionIdCookieNotFoundException;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.Session;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static stubidp.stubidp.StubIdpBinder.IS_SECURE_COOKIE_ENABLED;
import static stubidp.stubidp.cookies.CookieNames.SECURE_COOKIE_NAME;
import static stubidp.stubidp.cookies.CookieNames.SESSION_COOKIE_NAME;

/**
 * Use the "Synchronizer Token Pattern" to implement CSRF
 * see https://en.wikipedia.org/wiki/Cross-site_request_forgery
 */
public class CSRFCheckProtectionFilter implements ContainerRequestFilter {

    private final IdpSessionRepository idpSessionRepository;
    private final EidasSessionRepository eidasSessionRepository;
    private final HmacValidator hmacValidator;
    private final boolean isSecureCookieEnabled;

    public static final String CSRF_PROTECT_FORM_KEY = "csrf_protect";

    public enum Status {VERIFIED, ID_NOT_PRESENT, HASH_NOT_PRESENT, DELETED_SESSION, INVALID_HASH, NOT_FOUND }
    private static final String NO_CURRENT_SESSION_COOKIE_VALUE = "no-current-session";

    @Inject
    public CSRFCheckProtectionFilter(IdpSessionRepository idpSessionRepository,
                                     EidasSessionRepository eidasSessionRepository,
                                     HmacValidator hmacValidator,
                                     @Named(IS_SECURE_COOKIE_ENABLED) Boolean isSecureCookieEnabled) {
        this.idpSessionRepository = idpSessionRepository;
        this.eidasSessionRepository = eidasSessionRepository;
        this.hmacValidator = hmacValidator;
        this.isSecureCookieEnabled = isSecureCookieEnabled;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        final SessionId sessionId = getValidSessionId(requestContext);

        if(!requestContext.hasEntity()) {
            throw new CSRFBodyNotFoundException();
        }

        // slurp the request entity - treat it as an opaque string
        final String requestBody = new String(IOUtils.toByteArray(requestContext.getEntityStream()));

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

    private Optional<String> getTokenFromSession(SessionId sessionId) {
        final Optional<IdpSession> idpSession = idpSessionRepository.get(sessionId);
        final Optional<EidasSession> eidasSession = eidasSessionRepository.get(sessionId);
        return idpSession.map(Session::getCsrfToken).or(() -> eidasSession.map(Session::getCsrfToken));
    }

    private SessionId getValidSessionId(ContainerRequestContext requestContext) {

        // Get SessionId from cookie
        final Optional<String> sessionCookie = Optional.ofNullable(getValueOfPossiblyNullCookie(requestContext.getCookies(), SESSION_COOKIE_NAME));
        // Get SessionId HMAC from cookie
        final Optional<String> secureCookie;
        if (isSecureCookieEnabled) {
            secureCookie = Optional.ofNullable(getValueOfPossiblyNullCookie(requestContext.getCookies(), SECURE_COOKIE_NAME));
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

        if (StringUtils.isEmpty(sessionCookie.get())) {
            status = Status.ID_NOT_PRESENT;
        } else if (isSecureCookieEnabled && (secureCookie.isEmpty() || StringUtils.isEmpty(secureCookie.get()))) {
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

        if(status != Status.VERIFIED) {
            throw new CSRFCouldNotValidateSessionException(status);
        }

        return new SessionId(sessionCookie.get());
    }

    private String getValueOfPossiblyNullCookie(Map<String, Cookie> cookies, String cookieName) {
        return cookies.containsKey(cookieName) ? cookies.get(cookieName).getValue() : null;
    }

}
