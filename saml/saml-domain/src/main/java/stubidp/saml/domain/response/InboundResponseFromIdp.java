package stubidp.saml.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.domain.assertions.IdpIdaStatus;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public class InboundResponseFromIdp<Assertion> extends IdaSamlResponse {
    private Optional<Assertion> matchingDatasetAssertion;
    private Optional<Assertion> authnStatementAssertion;
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
            Optional<Assertion> matchingDatasetAssertion,
            URI destination,
            Optional<Assertion> authnStatementAssertion) {
        super(id, issueInstant, inResponseTo, issuer, destination);
        this.notOnOrAfter = notOnOrAfter;
        this.signature = signature;
        this.matchingDatasetAssertion = matchingDatasetAssertion;
        this.authnStatementAssertion = authnStatementAssertion;
        this.status = status;
    }

    public Optional<Assertion> getMatchingDatasetAssertion() {
        return matchingDatasetAssertion;
    }

    public Optional<Assertion> getAuthnStatementAssertion() {
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
