package stubidp.saml.hub.hub.test.builders;

import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.hub.hub.domain.AuthnRequestFromTransaction;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.hub.domain.IdaAuthnRequestFromHub;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;


public class IdaAuthnRequestBuilder {

    private String id = UUID.randomUUID().toString();
    private String issuer = "issuer_id";
    private Instant issueInstant = Instant.now();
    private List<AuthnContext> levelsOfAssurance = Collections.singletonList(AuthnContext.LEVEL_1);
    private Optional<Boolean> forceAuthentication = empty();
    private Optional<Integer> assertionConsumerServiceIndex = empty();
    private Optional<Signature> signature = empty();
    private URI destination = URI.create("http://thehub");
    private Optional<Instant> sessionExpiry = empty();
    private Optional<URI> assertionConsumerServiceUrl = empty();
    private AuthnContextComparisonTypeEnumeration comparisonType = AuthnContextComparisonTypeEnumeration.EXACT;

    public static IdaAuthnRequestBuilder anIdaAuthnRequest() {
        return new IdaAuthnRequestBuilder();
    }

    public IdaAuthnRequestFromHub buildFromHub() {
        return new IdaAuthnRequestFromHub(
                id,
                issuer,
                issueInstant,
                levelsOfAssurance,
                forceAuthentication,
                sessionExpiry.orElse(Instant.now().atZone(ZoneId.of("UTC")).plusHours(20).toInstant()),
                URI.create("/location"),
                comparisonType);
    }

    public AuthnRequestFromTransaction buildFromTransaction() {
        if (sessionExpiry.isPresent()) {
            throw new IllegalStateException("sessionExpiry can only be set on an authn request from hub");
        }
        return new AuthnRequestFromTransaction(
                id,
                issuer,
                issueInstant,
                forceAuthentication,
                assertionConsumerServiceUrl,
                assertionConsumerServiceIndex,
                signature,
                destination);
    }

    public IdaAuthnRequestBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public IdaAuthnRequestBuilder withIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    public IdaAuthnRequestBuilder withIssueInstant(Instant issueInstant) {
        this.issueInstant = issueInstant;
        return this;
    }

    public IdaAuthnRequestBuilder withLevelsOfAssurance(List<AuthnContext> levelsOfAssurance) {
        this.levelsOfAssurance = levelsOfAssurance;
        return this;
    }

    public IdaAuthnRequestBuilder withForceAuthentication(Optional<Boolean> forceAuthentication) {
        this.forceAuthentication = forceAuthentication;
        return this;
    }

    public IdaAuthnRequestBuilder withAssertionConsumerServiceIndex(int index) {
        this.assertionConsumerServiceIndex = ofNullable(index);
        return this;
    }

    public IdaAuthnRequestBuilder withAssertionConsumerServiceUrl(URI url) {
        this.assertionConsumerServiceUrl = ofNullable(url);
        return this;
    }

    public IdaAuthnRequestBuilder withoutAssertionConsumerServiceIndex() {
        this.assertionConsumerServiceIndex = empty();
        return this;
    }

    public IdaAuthnRequestBuilder withDestination(URI destination) {
        this.destination = destination;
        return this;
    }

    public IdaAuthnRequestBuilder withSessionExpiryTimestamp(Instant sessionExpiryTimestamp) {
        this.sessionExpiry = ofNullable(sessionExpiryTimestamp);
        return this;
    }

    public IdaAuthnRequestBuilder withSignature(Signature signature) {
        this.signature = ofNullable(signature);
        return this;
    }

    public IdaAuthnRequestBuilder withComparisonType(AuthnContextComparisonTypeEnumeration comparisonType) {
        this.comparisonType = comparisonType;
        return this;
    }
}
