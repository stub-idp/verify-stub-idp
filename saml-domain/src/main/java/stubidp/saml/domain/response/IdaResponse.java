package stubidp.saml.domain.response;

import java.time.Instant;

public interface IdaResponse {
    String getId();
    String getInResponseTo();
    Instant getIssueInstant();
    String getIssuer();
}
