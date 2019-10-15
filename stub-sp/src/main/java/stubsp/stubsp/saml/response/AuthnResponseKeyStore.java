package stubsp.stubsp.saml.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.hub.metadata.IdpMetadataPublicKeyStore;
import stubidp.saml.security.SigningKeyStore;

import javax.inject.Inject;
import java.security.PublicKey;
import java.util.List;

public class AuthnResponseKeyStore implements SigningKeyStore {

    private final Logger logger = LoggerFactory.getLogger(AuthnResponseKeyStore.class);
    private final IdpMetadataPublicKeyStore idpMetadataPublicKeyStore;

    @Inject
    public AuthnResponseKeyStore(IdpMetadataPublicKeyStore idpMetadataPublicKeyStore) {
        this.idpMetadataPublicKeyStore = idpMetadataPublicKeyStore;
    }

    @Override
    public List<PublicKey> getVerifyingKeysForEntity(String entityId) {
        logger.info("Requesting signature verifying key for {} in metadata", entityId);
        return idpMetadataPublicKeyStore.getVerifyingKeysForEntity(entityId);
    }
}
