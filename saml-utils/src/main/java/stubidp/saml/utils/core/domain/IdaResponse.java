package stubidp.saml.utils.core.domain;

import java.time.Instant;

public interface IdaResponse {
    String getId();
    String getInResponseTo();
    Instant getIssueInstant();
    String getIssuer();
}
