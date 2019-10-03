package stubidp.stubidp.saml.transformers;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;

import java.util.function.Function;

public class EidasResponseTransformerProvider {

    private final CoreTransformersFactory coreTransformersFactory;
    private final EncryptionKeyStore encryptionKeyStore;
    private final IdaKeyStore keyStore;
    private final EntityToEncryptForLocator entityToEncryptForLocator;
    private final SignatureAlgorithm signatureAlgorithm;
    private final DigestAlgorithm digestAlgorithm;

    public EidasResponseTransformerProvider(CoreTransformersFactory coreTransformersFactory,
                                            EncryptionKeyStore encryptionKeyStore,
                                            IdaKeyStore keyStore,
                                            EntityToEncryptForLocator entityToEncryptForLocator,
                                            SignatureAlgorithm signatureAlgorithm,
                                            DigestAlgorithm digestAlgorithm) {

        this.coreTransformersFactory = coreTransformersFactory;
        this.encryptionKeyStore = encryptionKeyStore;
        this.keyStore = keyStore;
        this.entityToEncryptForLocator = entityToEncryptForLocator;
        this.signatureAlgorithm = signatureAlgorithm;
        this.digestAlgorithm = digestAlgorithm;
    }

    public Function<Response, String> getTransformer(String issuerId) {
        return coreTransformersFactory.getResponseStringTransformer(
                encryptionKeyStore,
                keyStore,
                entityToEncryptForLocator,
                signatureAlgorithm,
                digestAlgorithm,
                new EncrypterFactory().withDataEncryptionAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM),
                issuerId);
    }

    public Function<Response, String> getUnsignedAssertionTransformer(String issuerId) {
        return coreTransformersFactory.getUnsignedAssertionResponseStringTransformer(
                encryptionKeyStore,
                keyStore,
                entityToEncryptForLocator,
                signatureAlgorithm,
                digestAlgorithm,
                new EncrypterFactory().withDataEncryptionAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM),
                issuerId);
    }
}
