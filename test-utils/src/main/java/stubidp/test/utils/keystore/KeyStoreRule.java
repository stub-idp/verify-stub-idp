package stubidp.test.utils.keystore;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.security.KeyStore;

public class KeyStoreRule implements BeforeEachCallback, AfterAllCallback {
    private final KeyStoreResource keyStoreResource;

    public KeyStore getKeyStore() {
        return keyStoreResource.getKeyStore();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        keyStoreResource.create();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        keyStoreResource.delete();
    }

    public String getAbsolutePath() {
        return keyStoreResource.getAbsolutePath();
    }

    public String getPassword() {
        return keyStoreResource.getPassword();
    }

    public File getFile() {
        return keyStoreResource.getFile();
    }

    public KeyStoreRule(KeyStoreResource keyStoreResource) {
        this.keyStoreResource = keyStoreResource;
    }
}
