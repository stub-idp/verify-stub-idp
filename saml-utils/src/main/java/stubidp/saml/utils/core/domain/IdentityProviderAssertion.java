package stubidp.saml.utils.core.domain;

import java.time.Instant;
import java.util.Optional;

public class IdentityProviderAssertion extends OutboundAssertion {
    private Optional<MatchingDataset> matchingDataset;
    private Optional<IdentityProviderAuthnStatement> authnStatement;

    public IdentityProviderAssertion(
            String id,
            String issuerId,
            Instant issueInstant,
            PersistentId persistentId,
            AssertionRestrictions assertionRestrictions,
            Optional<MatchingDataset> matchingDataset,
            Optional<IdentityProviderAuthnStatement> authnStatement) {

        super(id, issuerId, issueInstant, persistentId, assertionRestrictions);

        this.matchingDataset = matchingDataset;
        this.authnStatement = authnStatement;
    }

    public Optional<MatchingDataset> getMatchingDataset() {
        return matchingDataset;
    }

    public Optional<IdentityProviderAuthnStatement> getAuthnStatement(){
        return authnStatement;
    }
}
