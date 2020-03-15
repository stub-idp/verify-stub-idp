package stubidp.saml.hub.hub.domain;

import java.net.URI;
import java.time.Instant;

public abstract class VerifySamlMessage extends VerifyMessage {

    private URI destination;

    protected VerifySamlMessage() {
    }

    public VerifySamlMessage(String id, String issuer, Instant issueInstant, URI destination) {
        super(id, issuer, issueInstant);
        this.destination = destination;
    }

    public URI getDestination() {
        return destination;
    }
}
