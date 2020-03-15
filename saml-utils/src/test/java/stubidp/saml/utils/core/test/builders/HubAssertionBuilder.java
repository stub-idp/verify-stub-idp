package stubidp.saml.utils.core.test.builders;

import stubidp.saml.utils.core.domain.AssertionRestrictions;
import stubidp.saml.utils.core.domain.Cycle3Dataset;
import stubidp.saml.utils.core.domain.HubAssertion;
import stubidp.saml.utils.core.domain.PersistentId;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class HubAssertionBuilder {

    private String id = "assertion-id" + UUID.randomUUID();
    private String issuerId = "assertion issuer id";
    private Instant issueInstant = Instant.now();
    private PersistentId persistentId = PersistentIdBuilder.aPersistentId().build();
    private AssertionRestrictions assertionRestrictions = AssertionRestrictionsBuilder.anAssertionRestrictions().build();
    private Optional<Cycle3Dataset> cycle3Data = Optional.empty();

    public static HubAssertionBuilder aHubAssertion() {
        return new HubAssertionBuilder();
    }

    public HubAssertion build() {
        return new HubAssertion(
                id,
                issuerId,
                issueInstant,
                persistentId,
                assertionRestrictions,
                cycle3Data);
    }

    public HubAssertionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public HubAssertionBuilder withIssuerId(String issuerId) {
        this.issuerId = issuerId;
        return this;
    }

    public HubAssertionBuilder withIssueInstant(Instant issueInstant) {
        this.issueInstant = issueInstant;
        return this;
    }

    public HubAssertionBuilder withPersistentId(PersistentId persistentId) {
        this.persistentId = persistentId;
        return this;
    }

    public HubAssertionBuilder withAssertionRestrictions(AssertionRestrictions assertionRestrictions) {
        this.assertionRestrictions = assertionRestrictions;
        return this;
    }

    public HubAssertionBuilder withCycle3Data(Cycle3Dataset cycle3Data) {
        this.cycle3Data = Optional.ofNullable(cycle3Data);
        return this;
    }
}
