package stubidp.saml.metadata.factories;

import stubidp.saml.metadata.KeyStoreLoader;
import stubidp.saml.metadata.exception.EmptyTrustStoreException;

import javax.inject.Provider;
import java.security.KeyStore;
import java.security.KeyStoreException;

public class MetadataTrustStoreProvider implements Provider<KeyStore> {

    private final KeyStoreLoader keyStoreLoader;
    private final String uri;
    private final String password;

    public MetadataTrustStoreProvider(KeyStoreLoader keyStoreLoader, String uri, String password) {
        this.keyStoreLoader = keyStoreLoader;
        this.uri = uri;
        this.password = password;
    }

    @Override
    public KeyStore get() {
        KeyStore trustStore = keyStoreLoader.load(uri, password);
        int trustStoreSize;
        try {
            trustStoreSize = trustStore.size();
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        if (trustStoreSize == 0) {
            throw new EmptyTrustStoreException();
        }
        return trustStore;
    }
}
