package stubidp.saml.security;

import stubidp.saml.test.support.AbstractHardCodedKeyStore;

class HardCodedKeyStore extends AbstractHardCodedKeyStore implements SigningKeyStore, EncryptionKeyStore {
    HardCodedKeyStore(String entityId) {
        super(entityId);
    }
}
