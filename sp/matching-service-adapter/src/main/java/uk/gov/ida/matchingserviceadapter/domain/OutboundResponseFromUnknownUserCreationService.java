package uk.gov.ida.matchingserviceadapter.domain;

import stubidp.saml.domain.matching.IdaMatchingServiceResponse;
import stubidp.saml.domain.matching.UnknownUserCreationIdaStatus;

import java.time.Instant;
import java.util.Optional;

public class OutboundResponseFromUnknownUserCreationService extends IdaMatchingServiceResponse {
    private final UnknownUserCreationIdaStatus status;
    private final Optional<MatchingServiceAssertion> matchingServiceAssertion;

    public OutboundResponseFromUnknownUserCreationService(
            String responseId,
            String inResponseTo,
            String issuer,
            Instant issueInstant,
            UnknownUserCreationIdaStatus status,
            Optional<MatchingServiceAssertion> matchingServiceAssertion) {
        super(responseId, inResponseTo, issuer, issueInstant);
        this.status = status;
        this.matchingServiceAssertion = matchingServiceAssertion;
    }

    public static OutboundResponseFromUnknownUserCreationService createFailure(
            String responseId,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromUnknownUserCreationService(
                responseId,
                originalRequestId,
                issuerId,
                Instant.now(),
                UnknownUserCreationIdaStatus.CreateFailure,
                Optional.empty());
    }

    public static OutboundResponseFromUnknownUserCreationService createNoAttributeFailure(
            String responseId,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromUnknownUserCreationService(
                responseId,
                originalRequestId,
                issuerId,
                Instant.now(),
                UnknownUserCreationIdaStatus.NoAttributeFailure,
                Optional.empty());
    }

    public static OutboundResponseFromUnknownUserCreationService createSuccess(
            String responseId,
            MatchingServiceAssertion assertion,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromUnknownUserCreationService(
                responseId,
                originalRequestId,
                issuerId,
                Instant.now(),
                UnknownUserCreationIdaStatus.Success,
                Optional.ofNullable(assertion)
        );
    }

    public Optional<MatchingServiceAssertion> getMatchingServiceAssertion() {
        return matchingServiceAssertion;
    }

    public UnknownUserCreationIdaStatus getStatus() {
        return status;
    }
}
