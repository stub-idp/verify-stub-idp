package stubidp.stubidp.csrf;

import stubidp.shared.cookies.CookieNames;
import stubidp.shared.cookies.HmacValidator;
import stubidp.shared.csrf.AbstractCSRFCheckProtectionFilter;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.Session;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

public class StubIDPCSRFCheckProtectionFilter extends AbstractCSRFCheckProtectionFilter {

    private final IdpSessionRepository idpSessionRepository;
    private final EidasSessionRepository eidasSessionRepository;

    @Inject
    public StubIDPCSRFCheckProtectionFilter(IdpSessionRepository idpSessionRepository,
                                            EidasSessionRepository eidasSessionRepository,
                                            HmacValidator hmacValidator,
                                            @Named(IS_SECURE_COOKIE_ENABLED) Boolean isSecureCookieEnabled,
                                            CookieNames cookieNames) {
        super(hmacValidator, isSecureCookieEnabled, cookieNames);
        this.idpSessionRepository = idpSessionRepository;
        this.eidasSessionRepository = eidasSessionRepository;
    }

    protected Optional<String> getTokenFromSession(SessionId sessionId) {
        final Optional<IdpSession> idpSession = idpSessionRepository.get(sessionId);
        final Optional<EidasSession> eidasSession = eidasSessionRepository.get(sessionId);
        return idpSession.map(Session::getCsrfToken).or(() -> eidasSession.map(Session::getCsrfToken));
    }

    protected boolean sessionExists(SessionId sessionId) {
        return idpSessionRepository.containsSession(sessionId)
                || eidasSessionRepository.containsSession(sessionId);
    }
}
