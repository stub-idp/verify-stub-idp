package stubidp.saml.hub.validators.authnrequest;

import java.io.Serializable;

public record AuthnRequestIdKey(String requestId) implements Serializable {
}
