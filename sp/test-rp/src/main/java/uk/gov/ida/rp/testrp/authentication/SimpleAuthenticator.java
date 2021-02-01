package uk.gov.ida.rp.testrp.authentication;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;

import javax.inject.Inject;
import java.util.Optional;

public class SimpleAuthenticator implements Authenticator<SessionId, Session> {

    private final SessionRepository sessionRepository;

    @Inject
    public SimpleAuthenticator(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Optional<Session> authenticate(SessionId sessionId) throws AuthenticationException {
        return sessionRepository.getSession(sessionId);
    }
}
