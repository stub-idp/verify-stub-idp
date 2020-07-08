package stubidp.saml.security;

//Use KeyStoreBackedEncryptionCredentialResolver
@Deprecated
public class EncryptionCredentialFactory extends KeyStoreBackedEncryptionCredentialResolver {
    public EncryptionCredentialFactory(EncryptionKeyStore encryptionKeyStore) {
        super(encryptionKeyStore);
    }
}
