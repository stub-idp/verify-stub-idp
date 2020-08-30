package stubidp.test.utils.keystore;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

// DUPLICATE CLASS
class PrivateKeyFactory {

    public PrivateKey createPrivateKey(byte[] cert) {
        KeySpec keySpec = new PKCS8EncodedKeySpec(cert);
        KeyFactory keyFactory;

        try {
            keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }
}
