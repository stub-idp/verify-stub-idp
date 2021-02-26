package stubidp.stubidp.repositories;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.IdpHint;
import stubidp.stubidp.domain.IdpLanguageHint;
import stubidp.utils.rest.common.SessionId;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class IdpSession extends Session {
	private final IdaAuthnRequestFromHub idaAuthnRequestFromHub;
	private Optional<DatabaseIdpUser> idpUser = Optional.empty();
	private final Optional<UUID> singleIdpJourneyId;

	@JsonCreator
	public IdpSession(@JsonProperty("sessionId") SessionId sessionId,
					  @JsonProperty("startTime") Instant startTime,
					  @JsonProperty("idaAuthnRequestFromHub") IdaAuthnRequestFromHub idaAuthnRequestFromHub,
					  @JsonProperty("relayState") String relayState,
					  @JsonProperty("validHints") List<IdpHint> validHints,
					  @JsonProperty("invalidHints") List<String> invalidHints,
					  @JsonProperty("languageHint") Optional<IdpLanguageHint> languageHint,
					  @JsonProperty("registration") Optional<Boolean> registration,
				      @JsonProperty("singleIdpJourneyId") Optional<UUID> singleIdpJourneyId,
					  @JsonProperty("csrfToken") String csrfToken) {
		super(sessionId, startTime, relayState, validHints, invalidHints, languageHint, registration, csrfToken);
		this.idaAuthnRequestFromHub = idaAuthnRequestFromHub;
		this.singleIdpJourneyId = singleIdpJourneyId;
	}

	public IdpSession(SessionId sessionId,
					  Instant startTime,
					  IdaAuthnRequestFromHub idaAuthnRequestFromHub,
					  String relayState,
					  List<IdpHint> validHints,
					  List<String> invalidHints,
					  Optional<IdpLanguageHint> languageHint,
					  Optional<Boolean> registration) {
		super(sessionId, startTime, relayState, validHints, invalidHints, languageHint, registration, null);
		this.idaAuthnRequestFromHub = idaAuthnRequestFromHub;
		this.singleIdpJourneyId = Optional.empty();
	}

	public IdpSession(SessionId sessionId) {
		this(sessionId, Instant.now(), null, null, Collections.emptyList(), Collections.emptyList(), Optional.empty(), Optional.empty(), Optional.empty(), null);
	}

	public IdaAuthnRequestFromHub getIdaAuthnRequestFromHub() {
		return idaAuthnRequestFromHub;
	}

	public Optional<DatabaseIdpUser> getIdpUser() {
		return idpUser;
	}

	public void setIdpUser(Optional<DatabaseIdpUser> idpUser) {
		this.idpUser = idpUser;
	}

	public Optional<UUID> getSingleIdpJourneyId() {
		return singleIdpJourneyId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		IdpSession that = (IdpSession) o;
		return Objects.equals(idaAuthnRequestFromHub, that.idaAuthnRequestFromHub) && Objects.equals(idpUser, that.idpUser) && Objects.equals(singleIdpJourneyId, that.singleIdpJourneyId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), idaAuthnRequestFromHub, idpUser, singleIdpJourneyId);
	}

	@Override
	public String toString() {
		return "IdpSession{" +
				"idaAuthnRequestFromHub=" + idaAuthnRequestFromHub +
				", idpUser=" + idpUser +
				", singleIdpJourneyId=" + singleIdpJourneyId +
				'}';
	}
}
