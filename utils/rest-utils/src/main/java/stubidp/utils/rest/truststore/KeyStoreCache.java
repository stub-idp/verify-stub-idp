package stubidp.utils.rest.truststore;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.KeyStore;

@Singleton
public class KeyStoreCache {

    private final Cache<String, KeyStore> trustStoreCache;
    private final KeyStoreLoader keyStoreLoader;

    @Inject
    public KeyStoreCache(KeyStoreLoader keyStoreLoader) {
        this.keyStoreLoader = keyStoreLoader;
        this.trustStoreCache = Caffeine.newBuilder().build();
    }

    public KeyStore get(final ClientTrustStoreConfiguration configuration) {
        final String trustStorePath = configuration.getPath();
        final String password = configuration.getPassword();
        return trustStoreCache.get(trustStorePath, p -> keyStoreLoader.load(p, password));
    }
}
