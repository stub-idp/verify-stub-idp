package stubidp.saml.metadata;

import org.junit.jupiter.api.Test;
import stubidp.test.utils.helpers.ResourceHelpers;

import java.security.KeyStore;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyStoreLoaderTest {
    private KeyStoreLoader keyStoreLoader = new KeyStoreLoader();

    @Test
    public void testLoadFromString() throws Exception {
        KeyStore keyStore = keyStoreLoader.load(ResourceHelpers.resourceFilePath("test-truststore.ts"), "puppet");
        assertThat(keyStore).isNotNull();
    }

    @Test
    public void testLoadFromStream() throws Exception {
        KeyStore keyStore = keyStoreLoader.load(this.getClass().getResourceAsStream("/test-truststore.ts"), "puppet");
        assertThat(keyStore).isNotNull();
    }
}
