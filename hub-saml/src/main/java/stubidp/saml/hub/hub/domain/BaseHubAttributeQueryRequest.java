package stubidp.saml.hub.hub.domain;

import stubidp.saml.utils.core.domain.IdaSamlMessage;
import stubidp.saml.utils.core.domain.PersistentId;

import java.net.URI;
import java.time.Instant;

public class BaseHubAttributeQueryRequest extends IdaSamlMessage {
    protected PersistentId persistentId;
    protected URI assertionConsumerServiceUrl;
    protected String authnRequestIssuerEntityId;

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

