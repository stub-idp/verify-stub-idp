package stubidp.saml.hub.test.domain;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.hub.core.domain.MatchingServiceAuthnStatement;
import stubidp.saml.utils.core.domain.AssertionRestrictions;
import stubidp.saml.utils.core.domain.OutboundAssertion;
import stubidp.saml.utils.core.domain.PersistentId;

import java.time.Instant;
import java.util.List;

public class MatchingServiceAssertion extends OutboundAssertion {
    private MatchingServiceAuthnStatement authnStatement;
    private String audience;
    private List<Attribute> userAttributesForAccountCreation;

    public MatchingServiceAssertion(
            String id,
            String issuerId,
            Instant issueInstant,
            PersistentId persistentId,
            AssertionRestrictions assertionRestrictions,
            MatchingServiceAuthnStatement authnStatement,
            String audience,
            List<Attribute> userAttributesForAccountCreation) {

        super(id, issuerId, issueInstant, persistentId, assertionRestrictions);

        this.authnStatement = authnStatement;
        this.audience = audience;
        this.userAttributesForAccountCreation = userAttributesForAccountCreation;
    }

    public MatchingServiceAuthnStatement getAuthnStatement(){
        return authnStatement;
    }

    public String getAudience() {
        return audience;
    }

    public List<Attribute> getUserAttributesForAccountCreation() {
        return userAttributesForAccountCreation;
    }
}
