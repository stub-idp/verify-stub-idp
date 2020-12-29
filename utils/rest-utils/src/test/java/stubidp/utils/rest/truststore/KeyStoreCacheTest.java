package stubidp.utils.rest.truststore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KeyStoreCacheTest {

    @Mock
    private KeyStoreLoader keyStoreLoader;

    @Mock
    private KeyStore keyStore;

    private KeyStoreCache keyStoreCache;

    private ClientTrustStoreConfiguration configuration;

    @BeforeEach
    public void setUp(){
        keyStoreCache = new KeyStoreCache(keyStoreLoader);
        configuration = ClientTrustStoreConfigurationBuilder.aClientTrustStoreConfiguration().build();
    }

    @Test
    public void shouldLoadKeyStoreIfNotAlreadyLoaded() {
        when(keyStoreLoader.load(configuration.getPath(), configuration.getPassword())).thenReturn(keyStore);
        KeyStore keyStore = keyStoreCache.get(configuration);
        assertThat(keyStore).isEqualTo(this.keyStore);
    }

    @Test
    public void shouldOnlyHaveToLoadTheKeyStoreOnce() {
        when(keyStoreLoader.load(configuration.getPath(), configuration.getPassword())).thenReturn(keyStore);
        keyStoreCache.get(configuration);
        keyStoreCache.get(configuration);
        verify(keyStoreLoader, times(1)).load(configuration.getPath(), configuration.getPassword());
    }
}
