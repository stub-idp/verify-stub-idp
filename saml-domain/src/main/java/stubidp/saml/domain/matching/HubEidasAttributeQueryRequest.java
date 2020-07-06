package stubidp.saml.domain.matching;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.HubAssertion;
import stubidp.saml.domain.matching.BaseHubAttributeQueryRequest;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.domain.assertions.UserAccountCreationAttribute;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class HubEidasAttributeQueryRequest extends BaseHubAttributeQueryRequest {
    private final String encryptedIdentityAssertion;
    private final AuthnContext authnContext;
    private final Optional<HubAssertion> cycle3AttributeAssertion;
    private final Optional<List<UserAccountCreationAttribute>> userAccountCreationAttributes;

    public HubEidasAttributeQueryRequest(
            String requestId,
            String issuer,
            Instant issueInstant,
            PersistentId persistentId, // FIXME - change to use new PersistenceId with equal and hashcode at both places
            URI assertionConsumerServiceUrl,
            String authnRequestIssuerEntityId,
            String encryptedIdentityAssertion,
            AuthnContext authnContext,
            Optional<HubAssertion> cycle3AttributeAssertion,
            Optional<List<UserAccountCreationAttribute>> userAccountCreationAttributes) {

        super(requestId, issuer, issueInstant, null, persistentId, assertionConsumerServiceUrl, authnRequestIssuerEntityId);
        this.encryptedIdentityAssertion = encryptedIdentityAssertion;
        this.authnContext = authnContext;
        this.cycle3AttributeAssertion = cycle3AttributeAssertion;
        this.userAccountCreationAttributes = userAccountCreationAttributes;
    }

    public String getEncryptedIdentityAssertion() {
        return encryptedIdentityAssertion;
    }

    public AuthnContext getAuthnContext() {
        return authnContext;
    }

    public Optional<HubAssertion> getCycle3AttributeAssertion() {
        return cycle3AttributeAssertion;
    }

    public Optional<List<UserAccountCreationAttribute>> getUserAccountCreationAttributes() {
        return userAccountCreationAttributes;
    }
}
