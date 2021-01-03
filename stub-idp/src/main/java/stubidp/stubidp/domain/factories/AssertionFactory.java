package stubidp.stubidp.domain.factories;

import stubidp.saml.domain.assertions.FraudAuthnDetails;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.assertions.IpAddress;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.stubidp.domain.DatabaseIdpUser;

import javax.inject.Inject;
import java.time.Instant;

import static stubidp.saml.domain.assertions.IdentityProviderAuthnStatement.createIdentityProviderFraudAuthnStatement;

public class AssertionFactory {

    private final IdentityProviderAssertionFactory identityProviderAssertionFactory;
    private final AssertionRestrictionsFactory assertionRestrictionsFactory;

    @Inject
    public AssertionFactory(IdentityProviderAssertionFactory identityProviderAssertionFactory, AssertionRestrictionsFactory assertionRestrictionsFactory) {
        this.identityProviderAssertionFactory = identityProviderAssertionFactory;
        this.assertionRestrictionsFactory = assertionRestrictionsFactory;
    }

    private static PersistentId createPersistentId(String persistentId) {
        return new PersistentId(persistentId);
    }

    public IdentityProviderAssertion createFraudAuthnStatementAssertion(
            String issuerId,
            DatabaseIdpUser user,
            String inResponseToId,
            String idpName,
            String indicator,
            IpAddress userIpAddress) {

        return identityProviderAssertionFactory.createAuthnStatementAssertion(
                createPersistentId(user.getPersistentId()),
                issuerId,
                createIdentityProviderFraudAuthnStatement(new FraudAuthnDetails(idpName + Instant.now() + inResponseToId, indicator), userIpAddress),
                assertionRestrictionsFactory.createRestrictionsForSendingToHub(inResponseToId)
        );
    }

    public IdentityProviderAssertion createMatchingDatasetAssertion(String issuerId, DatabaseIdpUser user, String requestId) {
        MatchingDataset matchingDataset = MatchingDatasetFactory.create(user);
        return identityProviderAssertionFactory.createMatchingDatasetAssertion(
            createPersistentId(user.getPersistentId()),
            issuerId,
            matchingDataset,
            assertionRestrictionsFactory.createRestrictionsForSendingToHub(requestId));
    }
}
