package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.saml.domain.assertions.OutboundAssertion;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.domain.matching.assertions.MatchingServiceAuthnStatement;

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
