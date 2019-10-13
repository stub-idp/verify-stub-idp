package stubidp.stubidp.repositories;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.stubidp.domain.EidasAuthnRequest;
import stubidp.stubidp.domain.EidasUser;
import stubidp.stubidp.domain.IdpHint;
import stubidp.stubidp.domain.IdpLanguageHint;
import stubidp.utils.rest.common.SessionId;

import java.util.List;
import java.util.Optional;

public class EidasSession extends Session {
	private final EidasAuthnRequest eidasAuthnRequest;
	private Optional<EidasUser> eidasUser = Optional.empty();
	private boolean signAssertions = true;

	@JsonCreator
	public EidasSession(@JsonProperty("sessionId") SessionId sessionId,
						@JsonProperty("eidasAuthnRequest") EidasAuthnRequest eidasAuthnRequest,
						@JsonProperty("relayState") String relayState,
						@JsonProperty("validHints") List<IdpHint> validHints,
						@JsonProperty("invalidHints") List<String> invalidHints,
						@JsonProperty("languageHint") Optional<IdpLanguageHint> languageHint,
						@JsonProperty("registration") Optional<Boolean> registration,
						@JsonProperty("csrfToken") String csrfToken,
						@JsonProperty("assertionSigningIntention") boolean signAssertions) {
		super(sessionId, relayState, validHints, invalidHints, languageHint, registration, csrfToken);
		this.eidasAuthnRequest = eidasAuthnRequest;
		this.signAssertions = signAssertions;
	}

	public EidasSession(SessionId sessionId,
						EidasAuthnRequest eidasAuthnRequest,
						String relayState,
						List<IdpHint> validHints,
						List<String> invalidHints,
						Optional<IdpLanguageHint> languageHint,
						Optional<Boolean> registration) {
		super(sessionId, relayState, validHints, invalidHints, languageHint, registration, null);
		this.eidasAuthnRequest = eidasAuthnRequest;
	}

	public EidasAuthnRequest getEidasAuthnRequest() {
		return eidasAuthnRequest;
	}

	public Optional<EidasUser> getEidasUser() {
		return eidasUser;
	}

	public void setEidasUser(EidasUser eidasUser) {
		this.eidasUser = Optional.ofNullable(eidasUser);
	}

	public boolean getSignAssertions() {
		return signAssertions;
	}

	public void setSignAssertions(boolean signAssertions) {
		this.signAssertions = signAssertions;
	}
}
