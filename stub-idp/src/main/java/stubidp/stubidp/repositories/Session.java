package stubidp.stubidp.repositories;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.stubidp.domain.IdpHint;
import stubidp.stubidp.domain.IdpLanguageHint;
import stubidp.utils.rest.common.SessionId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Session {

    private final SessionId sessionId;
    private final Instant startTime;
    private final String relayState;
    private final List<IdpHint> validHints;
    private final List<String> invalidHints;
    private final Optional<IdpLanguageHint> languageHint;
    private final Optional<Boolean> registration;

    private String csrfToken;

    public Session(@JsonProperty("sessionId") SessionId sessionId,
                   @JsonProperty("startTime") Instant startTime,
                   @JsonProperty("relayState") String relayState,
                   @JsonProperty("validHints") List<IdpHint> validHints,
                   @JsonProperty("invalidHints") List<String> invalidHints,
                   @JsonProperty("languageHint") Optional<IdpLanguageHint> languageHint,
                   @JsonProperty("registration") Optional<Boolean> registration,
                   @JsonProperty("csrfToken") String csrfToken) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.relayState = relayState;
        this.validHints = validHints;
        this.invalidHints = invalidHints;
        this.languageHint = languageHint;
        this.registration = registration;
        this.csrfToken = csrfToken;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public String getRelayState() {
        return relayState;
    }

    public List<IdpHint> getValidHints() {
        return validHints;
    }

    public List<String> getInvalidHints() {
        return invalidHints;
    }

    public Optional<IdpLanguageHint> getLanguageHint() {
        return languageHint;
    }

    public Optional<Boolean> isRegistration() {
        return registration;
    }

    public String getCsrfToken() { return csrfToken; }

    @JsonIgnore
    public Session setNewCsrfToken() {
        this.csrfToken = UUID.randomUUID().toString();
        return this;
    }

    public String getStartTime() {
        return startTime.toString();
    }

    @JsonProperty("registration")
    public Optional<Boolean> getIsRegistration() {
        return registration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(sessionId, session.sessionId) && Objects.equals(startTime, session.startTime) && Objects.equals(relayState, session.relayState) && Objects.equals(validHints, session.validHints) && Objects.equals(invalidHints, session.invalidHints) && Objects.equals(languageHint, session.languageHint) && Objects.equals(registration, session.registration) && Objects.equals(csrfToken, session.csrfToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, startTime, relayState, validHints, invalidHints, languageHint, registration, csrfToken);
    }
}
