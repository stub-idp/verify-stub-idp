package stubidp.saml.domain.matching;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.HubAssertion;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.domain.assertions.UserAccountCreationAttribute;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class HubAttributeQueryRequest extends BaseHubAttributeQueryRequest {
    private final String encryptedAuthnAssertion;
    private final Optional<HubAssertion> cycle3AttributeAssertion;
    private final Optional<List<UserAccountCreationAttribute>> userAccountCreationAttributes;
    private final AuthnContext authnContext;
    private final String encryptedMatchingDatasetAssertion;

    public HubAttributeQueryRequest(
            String id,
            PersistentId persistentId,
            String encryptedMatchingDatasetAssertion,
            String encryptedAuthnAssertion,
            Optional<HubAssertion> cycle3AttributeAssertion,
            Optional<List<UserAccountCreationAttribute>> userAccountCreationAttributes,
            Instant issueInstant,
            URI assertionConsumerServiceUrl,
            String authnRequestIssuerEntityId,
            AuthnContext authnContext,
            String hubEntityId) {
        super(id, hubEntityId, issueInstant, null, persistentId, assertionConsumerServiceUrl, authnRequestIssuerEntityId);
        this.encryptedAuthnAssertion = encryptedAuthnAssertion;
        this.cycle3AttributeAssertion = cycle3AttributeAssertion;
        this.userAccountCreationAttributes = userAccountCreationAttributes;
        this.authnContext = authnContext;
        this.encryptedMatchingDatasetAssertion = encryptedMatchingDatasetAssertion;
    }

    public Optional<HubAssertion> getCycle3AttributeAssertion() {
        return cycle3AttributeAssertion;
    }

    public Optional<List<UserAccountCreationAttribute>> getUserAccountCreationAttributes() {
        return userAccountCreationAttributes;
    }

    public String getEncryptedAuthnAssertion() {
        return encryptedAuthnAssertion;
    }

    public AuthnContext getAuthnContext() {
        return authnContext;
    }

    public String getEncryptedMatchingDatasetAssertion() {
        return encryptedMatchingDatasetAssertion;
    }
}
