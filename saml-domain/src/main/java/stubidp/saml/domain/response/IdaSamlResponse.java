package stubidp.saml.domain.response;

import stubidp.saml.domain.IdaResponse;
import stubidp.saml.domain.IdaSamlMessage;

import java.net.URI;
import java.time.Instant;

public abstract class IdaSamlResponse extends IdaSamlMessage implements IdaResponse {

    private String inResponseTo;

    protected IdaSamlResponse() {}

    protected IdaSamlResponse(
            String responseId,
            Instant issueInstant,
            String inResponseTo,
            String issuer,
            URI destination) {
        super(responseId, issuer, issueInstant, destination);
        this.inResponseTo = inResponseTo;
    }

    public String getInResponseTo() {
        return inResponseTo;
    }
}
