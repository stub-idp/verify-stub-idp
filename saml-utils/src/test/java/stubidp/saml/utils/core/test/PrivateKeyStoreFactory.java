package stubidp.saml.utils.core.test;

import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PrivateKeyStore;

import java.security.PrivateKey;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class PrivateKeyStoreFactory {
    public PrivateKeyStore create(String entityId) {
        PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(entityId)));
        List<String> encryptionKeyStrings = TestCertificateStrings.PRIVATE_ENCRYPTION_KEYS.get(entityId);
        List<PrivateKey> privateEncryptionKeys = encryptionKeyStrings.stream()
            .map(input -> new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(input)))
            .collect(Collectors.toList());
        return new PrivateKeyStore(privateSigningKey, privateEncryptionKeys);
    }
}
