package stubidp.stubidp.security;

import stubidp.shared.repositories.MetadataRepository;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.saml.security.EncryptionKeyStore;

import java.security.PublicKey;

public class HubEncryptionKeyStore implements EncryptionKeyStore {

    private final MetadataRepository metadataRepository;
    private final PublicKeyFactory publicKeyFactory;

    public HubEncryptionKeyStore(MetadataRepository metadataRepository, PublicKeyFactory publicKeyFactory) {
        this.metadataRepository = metadataRepository;
        this.publicKeyFactory = publicKeyFactory;
    }

    @Override
    public PublicKey getEncryptionKeyForEntity(String entityId) {
        String encodedEncryptionCertificate = metadataRepository.getEncryptionCertificate();
        return publicKeyFactory.createPublicKey(encodedEncryptionCertificate);
    }
}
