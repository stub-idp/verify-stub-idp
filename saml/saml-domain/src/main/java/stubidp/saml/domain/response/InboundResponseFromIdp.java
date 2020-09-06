package stubidp.saml.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.domain.assertions.IdpIdaStatus;
import stubidp.saml.domain.assertions.PassthroughAssertion;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public class InboundResponseFromIdp extends IdaSamlResponse {
    private Optional<PassthroughAssertion> matchingDatasetAssertion;
    private Optional<PassthroughAssertion> authnStatementAssertion;
    private Optional<Instant> notOnOrAfter;
    private Optional<Signature> signature;
    private IdpIdaStatus status;

    public InboundResponseFromIdp(
            String id,
            String inResponseTo,
            String issuer,
            Instant issueInstant,
            Optional<Instant> notOnOrAfter,
            IdpIdaStatus status,
            Optional<Signature> signature,
            Optional<PassthroughAssertion> matchingDatasetAssertion,
            URI destination,
            Optional<PassthroughAssertion> authnStatementAssertion) {
        super(id, issueInstant, inResponseTo, issuer, destination);
        this.notOnOrAfter = notOnOrAfter;
        this.signature = signature;
        this.matchingDatasetAssertion = matchingDatasetAssertion;
        this.authnStatementAssertion = authnStatementAssertion;
        this.status = status;
    }

    public Optional<PassthroughAssertion> getMatchingDatasetAssertion() {
        return matchingDatasetAssertion;
    }

    public Optional<PassthroughAssertion> getAuthnStatementAssertion() {
        return authnStatementAssertion;
    }

    @JsonIgnore
    public Optional<Signature> getSignature() {
        return signature;
    }

    public IdpIdaStatus getStatus() {
        return status;
    }

    public Optional<Instant> getNotOnOrAfter() {
        return notOnOrAfter;
    }
}
