package stubidp.saml.domain.assertions;

import java.time.Instant;
import java.util.Optional;

public class HubAssertion extends OutboundAssertion {
    private final Optional<Cycle3Dataset> cycle3Data;

    public HubAssertion(
            String id,
            String issuerId,
            Instant issueInstant,
            PersistentId persistentId,
            AssertionRestrictions assertionRestrictions,
            Optional<Cycle3Dataset> cycle3Data) {

        super(id, issuerId, issueInstant, persistentId, assertionRestrictions);

        this.cycle3Data = cycle3Data;
    }

    public Optional<Cycle3Dataset> getCycle3Data() {
        return cycle3Data;
    }
}
