package uk.gov.ida.matchingserviceadapter.domain;

import stubidp.saml.domain.matching.IdaMatchingServiceResponse;
import stubidp.saml.domain.matching.MatchingServiceIdaStatus;

import java.time.Instant;
import java.util.Optional;

public class OutboundResponseFromMatchingService extends IdaMatchingServiceResponse {

    private Optional<MatchingServiceAssertion> matchingServiceAssertion;
    private MatchingServiceIdaStatus status;

    public OutboundResponseFromMatchingService(
            String responseId,
            String inResponseTo,
            String issuer,
            Instant issueInstant,
            MatchingServiceIdaStatus status,
            Optional<MatchingServiceAssertion> matchingServiceAssertion) {

        super(responseId, inResponseTo, issuer, issueInstant);

        this.matchingServiceAssertion = matchingServiceAssertion;
        this.status = status;
    }

    public Optional<MatchingServiceAssertion> getMatchingServiceAssertion() {
        return matchingServiceAssertion;
    }

    public static OutboundResponseFromMatchingService createMatchFromMatchingService(
            String responseId,
            MatchingServiceAssertion assertion,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromMatchingService(
                responseId,
                originalRequestId,
                issuerId,
                Instant.now(),
                MatchingServiceIdaStatus.MatchingServiceMatch,
                Optional.ofNullable(assertion)
        );
    }

    public static OutboundResponseFromMatchingService createNoMatchFromMatchingService(
            String responseId,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromMatchingService(
                responseId,
                originalRequestId,
                issuerId,
                Instant.now(),
                MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService,
                Optional.empty()
        );
    }

    public MatchingServiceIdaStatus getStatus() {
        return status;
    }
}
