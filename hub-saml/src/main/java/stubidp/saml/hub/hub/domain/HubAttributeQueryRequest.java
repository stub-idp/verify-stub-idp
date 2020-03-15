package stubidp.saml.hub.hub.domain;

import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.utils.core.domain.HubAssertion;
import stubidp.saml.utils.core.domain.PersistentId;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class HubAttributeQueryRequest extends BaseHubAttributeQueryRequest {
    private String encryptedAuthnAssertion;
    private Optional<HubAssertion> cycle3AttributeAssertion;
    private Optional<List<UserAccountCreationAttribute>> userAccountCreationAttributes;
    private AuthnContext authnContext;
    @NotNull
    private String encryptedMatchingDatasetAssertion;

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
