package uk.gov.ida.rp.testrp.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import java.security.KeyStore;

public class KeyStoreProvider implements Provider<KeyStore> {

    private final TrustStoreConfiguration configuration;

    @Inject
    public KeyStoreProvider(TrustStoreConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public KeyStore get() {return configuration.getTrustStore();
    }
}