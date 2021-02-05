package stubidp.saml.security;

import stubidp.saml.security.exception.SamlFailedToEncryptException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

import static stubidp.saml.security.errors.SamlTransformationErrorFactory.unableToEncryptXMLEncryptionKey;

public class SecretKeyEncrypter {

    private EncryptionCredentialResolver credentialFactory;

    public SecretKeyEncrypter(EncryptionCredentialResolver credentialFactory) {
        this.credentialFactory = credentialFactory;
    }

    public String encryptKeyForEntity(Key secretKey, String entityId) {
        PublicKey publicKey = credentialFactory.getEncryptingCredential(entityId).getPublicKey();
        try {
            Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.WRAP_MODE, publicKey);
            return Base64.getMimeEncoder().encodeToString(cipher.wrap(secretKey));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new SamlFailedToEncryptException(unableToEncryptXMLEncryptionKey(), e);
        }
    }
}