package stubidp.saml.hub.hub.domain;

import stubidp.saml.utils.core.domain.PersistentId;

import java.net.URI;
import java.time.Instant;

public class MatchingServiceHealthCheckRequest extends BaseHubAttributeQueryRequest {

    public MatchingServiceHealthCheckRequest(String id, Instant issueInstant, PersistentId persistentId, URI assertionConsumerServiceUrl, String authnRequestIssuerEntityId, String hubEntityId) {
        super(id, hubEntityId, issueInstant, null, persistentId, assertionConsumerServiceUrl, authnRequestIssuerEntityId);
    }
}
