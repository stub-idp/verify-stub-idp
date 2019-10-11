package stubidp.stubidp.domain;

import java.net.URI;

public interface SamlResponse {
    String getResponseString();
    String getRelayState();
    URI getSpSSOUrl();
}
