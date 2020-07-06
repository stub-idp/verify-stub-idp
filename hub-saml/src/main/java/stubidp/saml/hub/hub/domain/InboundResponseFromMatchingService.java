package stubidp.saml.hub.hub.domain;

import stubidp.saml.domain.matching.IdaMatchingServiceResponse;
import stubidp.saml.domain.assertions.PassthroughAssertion;
import stubidp.saml.hub.hub.transformers.inbound.MatchingServiceIdaStatus;

import java.time.Instant;
import java.util.Optional;

public class InboundResponseFromMatchingService extends IdaMatchingServiceResponse {
    private Optional<PassthroughAssertion> matchingServiceAssertion;
    private MatchingServiceIdaStatus status;

    @SuppressWarnings("unused") // needed for JAXB
    private InboundResponseFromMatchingService() {
    }

    public InboundResponseFromMatchingService(String responseId, String inResponseTo, String issuer, Instant issueInstant, MatchingServiceIdaStatus status, Optional<PassthroughAssertion> matchingServiceAssertion) {
        super(responseId, inResponseTo, issuer, issueInstant);
        this.matchingServiceAssertion = matchingServiceAssertion;
        this.status = status;
    }

    public Optional<PassthroughAssertion> getMatchingServiceAssertion() {
        return matchingServiceAssertion;
    }

    public MatchingServiceIdaStatus getStatus() {
        return status;
    }
}
