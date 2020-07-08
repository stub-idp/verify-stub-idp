package stubidp.saml.domain.matching;

import stubidp.saml.domain.matching.BaseHubAttributeQueryRequest;
import stubidp.saml.domain.assertions.PersistentId;

import java.net.URI;
import java.time.Instant;

public class MatchingServiceHealthCheckRequest extends BaseHubAttributeQueryRequest {
    public MatchingServiceHealthCheckRequest(String id, Instant issueInstant, PersistentId persistentId, URI assertionConsumerServiceUrl, String authnRequestIssuerEntityId, String hubEntityId) {
        super(id, hubEntityId, issueInstant, null, persistentId, assertionConsumerServiceUrl, authnRequestIssuerEntityId);
    }
}
