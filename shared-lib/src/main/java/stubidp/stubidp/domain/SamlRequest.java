package stubidp.stubidp.domain;

import java.net.URI;

public interface SamlRequest {
    String getRequestString();
    String getRelayState();
    URI getIdpSSOUrl();
}
