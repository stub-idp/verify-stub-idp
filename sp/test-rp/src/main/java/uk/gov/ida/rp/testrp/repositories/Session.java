package uk.gov.ida.rp.testrp.repositories;

import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.domain.JourneyHint;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.Principal;
import java.util.Optional;

import static uk.gov.ida.rp.testrp.Urls.Params.JOURNEY_HINT_PARAM;

public class Session implements Principal {

    private final String requestId;
    private final SessionId sessionId;
    private final String issuerId;
    private final Optional<Integer> assertionConsumerServiceIndex;
    private final Optional<JourneyHint> journeyHint;
    private final boolean forceAuthentication;
    private final boolean forceLMSUserAccountCreationFail;
    private final boolean forceLMSNoMatch;
    private final URI pathUserWasTryingToAccess;
    private Optional<String> matchedHashedPidForSession;

    public Session(SessionId sessionId, String requestId, URI pathUserWasTryingToAccess, String issuerId, Optional<Integer> assertionConsumerServiceIndex, Optional<JourneyHint> journeyHint, boolean forceAuthentication, boolean forceLMSNoMatch, boolean forceLMSUserAccountCreationFail) {
        this.sessionId = sessionId;
        this.requestId = requestId;
        // remove JOURNEY_HINT_PARAM, otherwise Cycle3UserFactory will bounce the user back to hub
        // when the user comes back and this is retrieved
        this.pathUserWasTryingToAccess = UriBuilder.fromUri(pathUserWasTryingToAccess).replaceQueryParam(JOURNEY_HINT_PARAM).build();
        this.issuerId = issuerId;
        this.assertionConsumerServiceIndex = assertionConsumerServiceIndex;
        this.journeyHint = journeyHint;
        this.forceAuthentication = forceAuthentication;
        this.forceLMSUserAccountCreationFail = forceLMSUserAccountCreationFail;
        this.forceLMSNoMatch = forceLMSNoMatch;
        this.matchedHashedPidForSession = Optional.empty();
    }

    public Session(SessionId sessionId, String requestId, URI pathUserWasTryingToAccess, String issuerId, Optional<Integer> assertionConsumerServiceIndex, Optional<JourneyHint> journeyHint) {
        this(sessionId, requestId, pathUserWasTryingToAccess, issuerId, assertionConsumerServiceIndex, journeyHint, false, false, false);
    }

    public boolean forceLMSUserAccountCreationFail() {
        return forceLMSUserAccountCreationFail;
    }

    public void setMatchedHashedPid(String matchedHashedPidForSession) {
        this.matchedHashedPidForSession = Optional.ofNullable(matchedHashedPidForSession);
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public boolean forceLMSNoMatch() {
        return forceLMSNoMatch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        if (forceAuthentication != session.forceAuthentication) return false;
        if (forceLMSUserAccountCreationFail != session.forceLMSUserAccountCreationFail) return false;
        if (forceLMSNoMatch != session.forceLMSNoMatch) return false;
        if (!requestId.equals(session.requestId)) return false;
        if (!sessionId.equals(session.sessionId)) return false;
        if (!issuerId.equals(session.issuerId)) return false;
        if (!assertionConsumerServiceIndex.equals(session.assertionConsumerServiceIndex)) return false;
        if (!journeyHint.equals(session.journeyHint)) return false;
        if (!pathUserWasTryingToAccess.equals(session.pathUserWasTryingToAccess)) return false;
        return matchedHashedPidForSession.equals(session.matchedHashedPidForSession);
    }

    @Override
    public int hashCode() {
        int result = requestId.hashCode();
        result = 31 * result + sessionId.hashCode();
        result = 31 * result + issuerId.hashCode();
        result = 31 * result + assertionConsumerServiceIndex.hashCode();
        result = 31 * result + journeyHint.hashCode();
        result = 31 * result + (forceAuthentication ? 1 : 0);
        result = 31 * result + (forceLMSUserAccountCreationFail ? 1 : 0);
        result = 31 * result + (forceLMSNoMatch ? 1 : 0);
        result = 31 * result + pathUserWasTryingToAccess.hashCode();
        result = 31 * result + matchedHashedPidForSession.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public String getName() {
        return "Session";
    }

    public URI getPathUserWasTryingToAccess() {
        return pathUserWasTryingToAccess;
    }

    public Optional<String> getMatchedHashedPidForSession() {
        return matchedHashedPidForSession;
    }

    public String getRequestId() {
        return requestId;
    }
}
