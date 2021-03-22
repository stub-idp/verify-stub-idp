package stubidp.utils.security.security;

import stubidp.utils.security.configuration.SecureCookieKeyStore;

import javax.crypto.Mac;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacDigest {

    private final HmacSha256MacFactory hmacSha256MacFactory;
    private final SecureCookieKeyStore secureCookieKeyStore;

    @Inject
    public HmacDigest(HmacSha256MacFactory hmacSha256MacFactory, SecureCookieKeyStore secureCookieKeyStore) {
        this.hmacSha256MacFactory = hmacSha256MacFactory;
        this.secureCookieKeyStore = secureCookieKeyStore;
    }

    public String digest(String toEncode) {
        Mac mac;
        try {
            mac = hmacSha256MacFactory.getInstance();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            mac.init(secureCookieKeyStore.getKey());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] bytes;
        bytes = mac.doFinal(toEncode.getBytes(StandardCharsets.UTF_8));

        return StringEncoding.toBase64Encoded(bytes);
    }

    public static class HmacSha256MacFactory {

        public HmacSha256MacFactory() {
        }

        public Mac getInstance() throws NoSuchAlgorithmException {
            return Mac.getInstance("HmacSHA256");
        }
    }
}
