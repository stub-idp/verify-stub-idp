package stubidp.saml.utils.core.domain;

import java.time.Instant;

public class AssertionRestrictions {
    private String recipient;
    private Instant notOnOrAfter;
    private String inResponseTo;

    protected AssertionRestrictions() {}

    public AssertionRestrictions(Instant notOnOrAfter, String inResponseTo, String recipient) {
        this.notOnOrAfter = notOnOrAfter;
        this.inResponseTo = inResponseTo;
        this.recipient = recipient;
    }

    public Instant getNotOnOrAfter() {
        return notOnOrAfter;
    }

    public String getInResponseTo() {
        return inResponseTo;
    }

    public String getRecipient() {
        return this.recipient;
    }
}
