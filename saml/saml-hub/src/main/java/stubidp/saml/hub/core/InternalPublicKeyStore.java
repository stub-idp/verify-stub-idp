package stubidp.saml.hub.core;

import java.security.PublicKey;
import java.util.List;

public interface InternalPublicKeyStore {
    List<PublicKey> getVerifyingKeysForEntity();
}
