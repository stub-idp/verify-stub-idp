package stubidp.saml.hub.hub.validators.authnrequest;

import java.time.Instant;

public interface IdExpirationCache<T> {
    boolean contains(T key);

    Instant getExpiration(T key);

    void setExpiration(T key, Instant dateTime);
}
