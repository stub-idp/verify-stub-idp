package stubidp.saml.domain.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import stubidp.saml.domain.IdaSamlMessage;
import stubidp.saml.domain.assertions.AuthnContext;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class IdaAuthnRequestFromHub extends IdaSamlMessage {
    private final List<AuthnContext> levelsOfAssurance;
    private final Optional<Boolean> forceAuthentication;
    private final Instant sessionExpiryTimestamp;
    private final AuthnContextComparisonTypeEnumeration comparisonType;

    @JsonCreator
    public IdaAuthnRequestFromHub(
            @JsonProperty("id") String id,
            @JsonProperty("issuer") String issuer,
            @JsonProperty("issueInstant") Instant issueInstant,
            @JsonProperty("levelsOfAssurance") List<AuthnContext> levelsOfAssurance,
            @JsonProperty("forceAuthentication") Optional<Boolean> forceAuthentication,
            @JsonProperty("sessionExpiryTimestamp") Instant sessionExpiryTimestamp,
            @JsonProperty("idpPostEndpoint") URI idpPostEndpoint,
            @JsonProperty("comparisonType") AuthnContextComparisonTypeEnumeration comparisonType) {
        super(id, issuer, issueInstant, idpPostEndpoint);
        this.levelsOfAssurance = levelsOfAssurance;
        this.forceAuthentication = forceAuthentication;
        this.sessionExpiryTimestamp = sessionExpiryTimestamp;
        this.comparisonType = comparisonType;
    }

    public static IdaAuthnRequestFromHub createRequestToSendFromHub(
            String id,
            List<AuthnContext> levelsOfAssurance,
            Optional<Boolean> forceAuthentication,
            Instant sessionExpiryTimestamp,
            URI idpPostEndpoint,
            AuthnContextComparisonTypeEnumeration comparisonType,
            String hubEntityId) {
        return new IdaAuthnRequestFromHub(id, hubEntityId, Instant.now(), levelsOfAssurance, forceAuthentication, sessionExpiryTimestamp, idpPostEndpoint, comparisonType);
    }

    public static IdaAuthnRequestFromHub createRequestReceivedFromHub(String id, String issuerId, List<AuthnContext> levelsOfAssurance, boolean forceAuthentication, Instant notOnOrAfter, AuthnContextComparisonTypeEnumeration comparisonType) {
        return new IdaAuthnRequestFromHub(id, issuerId, Instant.now(), levelsOfAssurance, Optional.of(forceAuthentication), notOnOrAfter, null, comparisonType); // null because it was sent to the consumer of this message
    }

    public Optional<Boolean> getForceAuthentication() {
        return forceAuthentication;
    }

    public Instant getSessionExpiryTimestamp() {
        return sessionExpiryTimestamp;
    }

    public List<AuthnContext> getLevelsOfAssurance() {
        return levelsOfAssurance;
    }

    public AuthnContextComparisonTypeEnumeration getComparisonType() {
        return comparisonType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdaAuthnRequestFromHub that = (IdaAuthnRequestFromHub) o;
        return Objects.equals(levelsOfAssurance, that.levelsOfAssurance) && Objects.equals(forceAuthentication, that.forceAuthentication) && Objects.equals(sessionExpiryTimestamp, that.sessionExpiryTimestamp) && comparisonType == that.comparisonType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(levelsOfAssurance, forceAuthentication, sessionExpiryTimestamp, comparisonType);
    }

    @Override
    public String toString() {
        return "IdaAuthnRequestFromHub{" +
                "levelsOfAssurance=" + levelsOfAssurance +
                ", forceAuthentication=" + forceAuthentication +
                ", sessionExpiryTimestamp=" + sessionExpiryTimestamp +
                ", comparisonType=" + comparisonType +
                '}';
    }
}
