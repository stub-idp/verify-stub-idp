package uk.gov.ida.rp.testrp.providers;

import stubidp.saml.metadata.TrustStoreConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
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