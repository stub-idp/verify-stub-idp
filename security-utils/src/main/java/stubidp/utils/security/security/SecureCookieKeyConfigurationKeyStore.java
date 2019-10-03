package stubidp.utils.security.security;

import com.google.common.base.Strings;
import stubidp.utils.security.configuration.KeyConfiguration;
import stubidp.utils.security.configuration.SecureCookieConfiguration;
import stubidp.utils.security.configuration.SecureCookieKeyStore;

import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Base64;

public class SecureCookieKeyConfigurationKeyStore implements SecureCookieKeyStore {

    private KeyConfiguration keyConfiguration;

    @Inject
    public SecureCookieKeyConfigurationKeyStore(SecureCookieConfiguration keyConfiguration) {
        this.keyConfiguration = keyConfiguration.getKeyConfiguration();
    }

    @Override
    public Key getKey() {
        try {
            if(Strings.isNullOrEmpty(keyConfiguration.getBase64EncodedKey())) {
                String keyUri = keyConfiguration.getKeyUri();
                return getSecureCookieKey(keyUri);
            } else {
                return new SecretKeySpec(Base64.getDecoder().decode(keyConfiguration.getBase64EncodedKey()), "HmacSHA1");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Key getSecureCookieKey(String keyUri) throws IOException {
        try(InputStream inputStream = new FileInputStream(new File(keyUri))) {
            byte[] ous = FileUtils.readStream(inputStream);
            return new SecretKeySpec(ous, "HmacSHA1");
        }
    }
}
