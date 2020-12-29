package stubidp.saml.hub.validators.authnrequest;

import java.io.Serializable;
import java.util.Objects;

public class AuthnRequestIdKey implements Serializable {
    private final String requestId;

    AuthnRequestIdKey(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthnRequestIdKey that = (AuthnRequestIdKey) o;
        return Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }
}
