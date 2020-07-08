package stubidp.saml.security;

import stubidp.saml.test.support.AbstractHardCodedKeyStore;

public class HardCodedKeyStore extends AbstractHardCodedKeyStore implements SigningKeyStore, EncryptionKeyStore {
    public HardCodedKeyStore(String entityId) {
        super(entityId);
    }
}
