package stubidp.stubidp.security;

import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.SigningKeyStore;
import stubidp.saml.test.support.AbstractHardCodedKeyStore;

public class HardCodedKeyStore extends AbstractHardCodedKeyStore implements SigningKeyStore, EncryptionKeyStore {
    public HardCodedKeyStore(String entityId) {
        super(entityId);
    }
}
