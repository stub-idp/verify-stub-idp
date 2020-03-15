package stubidp.saml.hub.hub.domain;

import stubidp.saml.hub.core.domain.IdaMatchingServiceResponse;
import stubidp.saml.hub.hub.transformers.inbound.MatchingServiceIdaStatus;

import java.time.Instant;

public class InboundHealthCheckResponseFromMatchingService extends IdaMatchingServiceResponse {
    private MatchingServiceIdaStatus status;

    @SuppressWarnings("unused") // needed for JAXB
    private InboundHealthCheckResponseFromMatchingService() {
    }

    public InboundHealthCheckResponseFromMatchingService(
            final String responseId,
            final String inResponseTo,
            final String issuer,
            final Instant issueInstant,
            final MatchingServiceIdaStatus status) {

        super(responseId, inResponseTo, issuer, issueInstant);

        this.status = status;
    }

    public MatchingServiceIdaStatus getStatus() {
        return status;
    }
}
