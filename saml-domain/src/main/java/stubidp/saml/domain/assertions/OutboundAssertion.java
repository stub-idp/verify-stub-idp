package stubidp.saml.domain.assertions;

import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.saml.domain.assertions.PersistentId;

import java.time.Instant;

public class OutboundAssertion {
    private final String id;
    private final String issuerId;
    private final Instant issueInstant;
    private final PersistentId persistentId;
    private final AssertionRestrictions assertionRestrictions;

    public OutboundAssertion(
            String id,
            String issuerId,
            Instant issueInstant,
            PersistentId persistentId,
            AssertionRestrictions assertionRestrictions) {

        this.id = id;
        this.issuerId = issuerId;
        this.issueInstant = issueInstant;
        this.persistentId = persistentId;
        this.assertionRestrictions = assertionRestrictions;
    }

    public PersistentId getPersistentId() {
        return persistentId;
    }

    public AssertionRestrictions getAssertionRestrictions() {
        return assertionRestrictions;
    }

    public String getId() {
        return id;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public Instant getIssueInstant() {
        return issueInstant;
    }
}
