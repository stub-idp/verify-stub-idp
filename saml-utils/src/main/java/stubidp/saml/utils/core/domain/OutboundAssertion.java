package stubidp.saml.utils.core.domain;

import java.time.Instant;

public class OutboundAssertion {

    private String id;
    private String issuerId;
    private Instant issueInstant;
    private PersistentId persistentId;
    private AssertionRestrictions assertionRestrictions;

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
