package stubidp.saml.hub.core.domain;

import stubidp.saml.utils.core.domain.IdaMessage;
import stubidp.saml.utils.core.domain.IdaResponse;

import java.time.Instant;

public abstract class IdaMatchingServiceResponse extends IdaMessage implements IdaResponse {

    private String inResponseTo;

    protected IdaMatchingServiceResponse() {
    }

    public IdaMatchingServiceResponse(String responseId, String inResponseTo, String issuer, Instant issueInstant) {
        super(responseId, issuer, issueInstant);
        this.inResponseTo = inResponseTo;
    }

    public String getInResponseTo() {
        return inResponseTo;
    }
}
