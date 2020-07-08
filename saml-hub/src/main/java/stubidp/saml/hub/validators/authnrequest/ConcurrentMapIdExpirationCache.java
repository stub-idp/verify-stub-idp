package stubidp.saml.hub.validators.authnrequest;

import java.time.Instant;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentMapIdExpirationCache<T> implements IdExpirationCache<T> {
    private final ConcurrentMap<T, Instant> infinispanMap;

    public ConcurrentMapIdExpirationCache(ConcurrentMap<T, Instant> infinispanMap) {
        this.infinispanMap = infinispanMap;
    }

    @Override
    public boolean contains(T key) {
        return infinispanMap.containsKey(key);
    }

    @Override
    public Instant getExpiration(T key) {
        return infinispanMap.get(key);
    }

    @Override
    public void setExpiration(T key, Instant expirationTime) {
        infinispanMap.put(key, expirationTime);
    }

    public long getKeyCount() {
        return infinispanMap.keySet().size();
    }
}
