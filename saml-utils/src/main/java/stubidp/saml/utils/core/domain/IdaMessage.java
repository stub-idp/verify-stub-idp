package stubidp.saml.utils.core.domain;

import java.time.Instant;

public abstract class IdaMessage {

    private String id;
    private String issuer;
    private Instant issueInstant;

    protected IdaMessage() {
    }

    public IdaMessage(String id, String issuer, Instant issueInstant) {
        this.id = id;
        this.issuer = issuer;
        this.issueInstant = issueInstant;
    }

    public String getId(){
        return id;
    }

    public String getIssuer() {
        return issuer;
    }

    public Instant getIssueInstant() {
        return issueInstant;
    }
}
