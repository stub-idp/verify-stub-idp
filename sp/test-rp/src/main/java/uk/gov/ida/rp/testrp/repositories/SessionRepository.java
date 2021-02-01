package uk.gov.ida.rp.testrp.repositories;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import stubidp.utils.rest.common.SessionId;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationResponseDto;
import uk.gov.ida.rp.testrp.domain.JourneyHint;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static uk.gov.ida.rp.testrp.contract.UnknownUserCreationResponseDto.FAILURE_RESPONSE;
import static uk.gov.ida.rp.testrp.contract.UnknownUserCreationResponseDto.SUCCESS_RESPONSE;

@Singleton
public class SessionRepository {

    private final Cache<SessionId, Session> sessions;

    @Inject
    public SessionRepository(@Named("sessionCacheTimeoutInMinutes") Integer sessionCacheTimeoutInMinutes) {
        this.sessions = Caffeine.newBuilder()
                .expireAfterWrite(sessionCacheTimeoutInMinutes, TimeUnit.MINUTES)
                .build();
    }

    public Optional<Session> getSession(SessionId sessionId) {
        if (!sessions.asMap().containsKey(sessionId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.asMap().get(sessionId));
    }

    public SessionId newSession(String requestId, URI requestUri, String issuerId, Optional<Integer> assertionConsumerServiceIndex, Optional<JourneyHint> journeyHint, boolean forceAuthentication, boolean forceLMSNoMatch, boolean forceLMSUserAccountCreationFail) {
        SessionId sessionId = SessionId.createNewSessionId();
        Session session = new Session(sessionId, requestId, requestUri, issuerId, assertionConsumerServiceIndex, journeyHint, forceAuthentication, forceLMSNoMatch, forceLMSUserAccountCreationFail);
        updateSession(sessionId, session);
        return sessionId;
    }

    public void updateSession(SessionId id, Session session) {
        sessions.put(id, session);
    }

    public void delete(SessionId sessionId) {
        sessions.invalidate(sessionId);
    }

    public Optional<Session> getSessionForRequestId(String requestId) {
        return sessions.asMap().values().stream()
                .filter(s -> s.getRequestId().equals(requestId))
                .findFirst();
    }

    /**
     * There is potentially a nasty race condition with this method when multiple people are logging in with the same
     * user (in test environments) - as the wrong user could be linked to the wrong UnknownUserCreationResponseDto
     * but the worst that would happen is that the user would get the wrong response from the LMS for user account creation.
     *
     * We could fix this well enough by making randomPid default to true in stub-idp.
     *
     * For a real fix the internal LMS needs to get the requestId from the MSA...  This is potentially an issue that real
     * relying parties might come across.
     */
    public Optional<UnknownUserCreationResponseDto> getResponseFromSessionForHashedPid(String hashedPid) {
        return sessions.asMap().values().stream()
                .filter(s -> s.getMatchedHashedPidForSession().isPresent() && s.getMatchedHashedPidForSession().get().equals(hashedPid))
                .findFirst()
                .map(s -> s.forceLMSUserAccountCreationFail()?FAILURE_RESPONSE:SUCCESS_RESPONSE);
    }
}
