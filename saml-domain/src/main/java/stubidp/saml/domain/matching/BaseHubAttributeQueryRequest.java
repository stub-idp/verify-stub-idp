package stubidp.saml.domain.matching;

import stubidp.saml.domain.IdaSamlMessage;
import stubidp.saml.domain.assertions.PersistentId;

import java.net.URI;
import java.time.Instant;

public class BaseHubAttributeQueryRequest extends IdaSamlMessage {
    protected final PersistentId persistentId;
    protected final URI assertionConsumerServiceUrl;
    protected final String authnRequestIssuerEntityId;

    public BaseHubAttributeQueryRequest(String id,
                                        String issuer,
                                        Instant issueInstant,
                                        URI destination,
                                        PersistentId persistentId,
                                        URI assertionConsumerServiceUrl,
                                        String authnRequestIssuerEntityId) {
        super(id, issuer, issueInstant, destination);
        this.persistentId = persistentId;
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        this.authnRequestIssuerEntityId = authnRequestIssuerEntityId;
    }

    public PersistentId getPersistentId() {
        return persistentId;
    }

    public URI getAssertionConsumerServiceUrl() {
        return assertionConsumerServiceUrl;
    }

    public String getAuthnRequestIssuerEntityId() {
        return authnRequestIssuerEntityId;
    }

}

