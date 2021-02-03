package stubidp.saml.security;

//Use KeyStoreBackedEncryptionCredentialResolver
public class EncryptionCredentialFactory extends KeyStoreBackedEncryptionCredentialResolver {
    public EncryptionCredentialFactory(EncryptionKeyStore encryptionKeyStore) {
        super(encryptionKeyStore);
    }
}
