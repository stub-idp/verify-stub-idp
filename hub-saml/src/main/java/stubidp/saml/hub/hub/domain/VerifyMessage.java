package stubidp.saml.hub.hub.domain;

import java.time.Instant;

public abstract class VerifyMessage {

    private String id;
    private String issuer;
    private Instant issueInstant;

    protected VerifyMessage() {
    }

    public VerifyMessage(String id, String issuer, Instant issueInstant) {
        this.id = id;
        this.issuer = issuer;
        this.issueInstant = issueInstant;
    }

    public String getId() {
        return id;
    }

    public String getIssuer() {
        return issuer;
    }

    public Instant getIssueInstant() {
        return issueInstant;
    }
}
