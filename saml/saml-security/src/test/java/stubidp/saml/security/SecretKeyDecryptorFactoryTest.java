package stubidp.saml.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.security.credential.Credential;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.test.devpki.TestCertificateStrings;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretKeyDecryptorFactoryTest {

    private final Credential credential = new TestCredentialFactory(
            TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT,
            TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY)
            .getEncryptionKeyPair();

    @InjectMocks
    private SecretKeyDecryptorFactory factory;

    @Mock
    private IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever;

    @Mock
    private Credential encryptionCredentials;

    @Test
    void shouldCreateDecreypterUsingPrivateKey() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
        SecretKey secretKey = createSecretKey();
        String encryptedSecretKey = encryptSecretKeyWithCredentialsPublicKey(secretKey);
        when(idaKeyStoreCredentialRetriever.getDecryptingCredentials()).thenReturn(List.of(encryptionCredentials));
        when(encryptionCredentials.getPrivateKey()).thenReturn(credential.getPrivateKey());
        factory.createDecrypter(encryptedSecretKey);
        verify(idaKeyStoreCredentialRetriever).getDecryptingCredentials();
        verify(encryptionCredentials).getPrivateKey();
    }

    private String encryptSecretKeyWithCredentialsPublicKey(SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
        PublicKey publicKey = credential.getPublicKey();
        Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.wrap(secretKey));
    }

    private SecretKey createSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56);
        return keyGenerator.generateKey();
    }
} 