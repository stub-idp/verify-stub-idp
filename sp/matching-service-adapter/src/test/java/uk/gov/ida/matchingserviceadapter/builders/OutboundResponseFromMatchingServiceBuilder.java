package uk.gov.ida.matchingserviceadapter.builders;

import stubidp.saml.domain.matching.MatchingServiceIdaStatus;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;

import java.time.Instant;
import java.util.Optional;

public class OutboundResponseFromMatchingServiceBuilder {

    protected String responseId = null;
    protected Instant issueInstant = null;
    protected String inResponseTo = "request-id";
    protected String issuerId = null;
    protected MatchingServiceIdaStatus status = null;

    private Optional<MatchingServiceAssertion> matchingServiceAssertion = Optional.empty();

    private OutboundResponseFromMatchingServiceBuilder() {
        withResponseId("response-id");
        withInResponseTo("request-id");
        withIssuerId("issuer-id");
        withIssueInstant(Instant.now());
        withStatus(MatchingServiceIdaStatus.MatchingServiceMatch);
    }

    public static OutboundResponseFromMatchingServiceBuilder aResponse() {
        return new OutboundResponseFromMatchingServiceBuilder();
    }

    public OutboundResponseFromMatchingServiceBuilder withResponseId(String responseId) {
        this.responseId = responseId;
        return this;
    }

    public OutboundResponseFromMatchingServiceBuilder withIssueInstant(Instant issueInstant) {
        this.issueInstant = issueInstant;
        return this;
    }

    public OutboundResponseFromMatchingServiceBuilder withInResponseTo(String inResponseTo) {
        this.inResponseTo = inResponseTo;
        return this;
    }

    public OutboundResponseFromMatchingServiceBuilder withIssuerId(String issuer) {
        this.issuerId = issuer;
        return this;
    }

    public OutboundResponseFromMatchingServiceBuilder withStatus(MatchingServiceIdaStatus status) {
        this.status = status;
        return this;
    }

    public OutboundResponseFromMatchingService build() {
        return new OutboundResponseFromMatchingService(
                responseId,
                inResponseTo,
                issuerId,
                issueInstant,
                status,
                matchingServiceAssertion);
    }
}
