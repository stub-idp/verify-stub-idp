package stubidp.saml.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.security.credential.Credential;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.test.devpki.TestCertificateStrings;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretKeyEncrypterTest {
    
    @Mock
    private KeyStoreBackedEncryptionCredentialResolver credentialResolver;

    private static final String AN_ENTITY_ID = "ministry-of-pies";

    private final Credential credential = new TestCredentialFactory(
            TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT,
            TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY)
            .getEncryptionKeyPair();

    @Test
    void shouldSuccessfullyEncryptASecretKey() throws Exception {
        SecretKeyEncrypter testSubject;

        when(credentialResolver.getEncryptingCredential(AN_ENTITY_ID)).thenReturn(credential);

        testSubject = new SecretKeyEncrypter(credentialResolver);

        SecretKey unEncryptedSecretKey = getSecretKey();

        String encryptedSecretKey = testSubject.encryptKeyForEntity(unEncryptedSecretKey, AN_ENTITY_ID);

        Key decryptedSecretKey = decryptSecretKey(encryptedSecretKey);
        assertThat(decryptedSecretKey.getEncoded()).isEqualTo(unEncryptedSecretKey.getEncoded());
    }

    private SecretKey getSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56);
        return keyGenerator.generateKey();
    }

    private Key decryptSecretKey(String base64EncryptedSecretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.UNWRAP_MODE, credential.getPrivateKey());
        return cipher.unwrap(Base64.getMimeDecoder().decode(base64EncryptedSecretKey), "RSA", Cipher.SECRET_KEY);
    }

}