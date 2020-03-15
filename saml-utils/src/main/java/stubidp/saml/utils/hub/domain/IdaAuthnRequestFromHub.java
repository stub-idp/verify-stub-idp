package stubidp.saml.utils.hub.domain;

import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.core.domain.IdaSamlMessage;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class IdaAuthnRequestFromHub extends IdaSamlMessage {
    private List<AuthnContext> levelsOfAssurance;
    private Optional<Boolean> forceAuthentication;
    private Instant sessionExpiryTimestamp;
    private final AuthnContextComparisonTypeEnumeration comparisonType;

    public IdaAuthnRequestFromHub(
            String id,
            String issuer,
            Instant issueInstant,
            List<AuthnContext> levelsOfAssurance,
            Optional<Boolean> forceAuthentication,
            Instant sessionExpiryTimestamp,
            URI idpPostEndpoint,
            AuthnContextComparisonTypeEnumeration comparisonType) {
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
}
