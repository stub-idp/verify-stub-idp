package uk.gov.ida.rp.testrp.repositories;

import stubidp.utils.rest.common.SessionId;
import uk.gov.ida.rp.testrp.domain.JourneyHint;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.Principal;
import java.util.Objects;
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
        return forceAuthentication == session.forceAuthentication && forceLMSUserAccountCreationFail == session.forceLMSUserAccountCreationFail && forceLMSNoMatch == session.forceLMSNoMatch && Objects.equals(requestId, session.requestId) && Objects.equals(sessionId, session.sessionId) && Objects.equals(issuerId, session.issuerId) && Objects.equals(assertionConsumerServiceIndex, session.assertionConsumerServiceIndex) && Objects.equals(journeyHint, session.journeyHint) && Objects.equals(pathUserWasTryingToAccess, session.pathUserWasTryingToAccess) && Objects.equals(matchedHashedPidForSession, session.matchedHashedPidForSession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, sessionId, issuerId, assertionConsumerServiceIndex, journeyHint, forceAuthentication, forceLMSUserAccountCreationFail, forceLMSNoMatch, pathUserWasTryingToAccess, matchedHashedPidForSession);
    }

    @Override
    public String toString() {
        return "Session{" +
                "requestId='" + requestId + '\'' +
                ", sessionId=" + sessionId +
                ", issuerId='" + issuerId + '\'' +
                ", assertionConsumerServiceIndex=" + assertionConsumerServiceIndex +
                ", journeyHint=" + journeyHint +
                ", forceAuthentication=" + forceAuthentication +
                ", forceLMSUserAccountCreationFail=" + forceLMSUserAccountCreationFail +
                ", forceLMSNoMatch=" + forceLMSNoMatch +
                ", pathUserWasTryingToAccess=" + pathUserWasTryingToAccess +
                ", matchedHashedPidForSession=" + matchedHashedPidForSession +
                '}';
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
