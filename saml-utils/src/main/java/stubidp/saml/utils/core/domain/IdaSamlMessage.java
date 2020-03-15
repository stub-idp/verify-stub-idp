package stubidp.saml.utils.core.domain;

import java.net.URI;
import java.time.Instant;

public abstract class IdaSamlMessage extends IdaMessage {

    private URI destination;

    protected IdaSamlMessage() {
    }

    public IdaSamlMessage(String id, String issuer, Instant issueInstant, URI destination) {
        super(id, issuer, issueInstant);
        this.destination = destination;
    }

    public URI getDestination() {
        return destination;
    }
}
