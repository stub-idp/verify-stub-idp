package stubidp.stubidp.saml.transformers.outbound;

import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.domain.response.OutboundResponseFromIdp;
import stubidp.stubidp.domain.factories.StubTransformersFactory;
import stubidp.stubidp.repositories.Idp;

import java.util.Optional;
import java.util.function.Function;

public class OutboundResponseFromIdpTransformerProvider {
    private final EncryptionKeyStore encryptionKeyStore;
    private final IdaKeyStore keyStore;
    private final EntityToEncryptForLocator entityToEncryptForLocator;
    private final Optional<String> publicSigningKey;
    private final StubTransformersFactory stubTransformersFactory;
    private final SignatureAlgorithm signatureAlgorithm;
    private final DigestAlgorithm digestAlgorithm;

    public OutboundResponseFromIdpTransformerProvider(
            EncryptionKeyStore encryptionKeyStore,
            IdaKeyStore keyStore,
            EntityToEncryptForLocator entityToEncryptForLocator,
            Optional<String> publicSigningKey,
            StubTransformersFactory stubTransformersFactory,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm) {
        this.encryptionKeyStore = encryptionKeyStore;
        this.keyStore = keyStore;
        this.entityToEncryptForLocator = entityToEncryptForLocator;
        this.publicSigningKey = publicSigningKey;
        this.stubTransformersFactory = stubTransformersFactory;
        this.signatureAlgorithm = signatureAlgorithm;
        this.digestAlgorithm = digestAlgorithm;
    }

    private Function<OutboundResponseFromIdp, String> getTransformerWithKeyInfo(String issuerId) {
        return stubTransformersFactory.getOutboundResponseFromIdpToStringTransformer(
                encryptionKeyStore,
                keyStore,
                entityToEncryptForLocator,
                publicSigningKey.get(),
                issuerId,
                signatureAlgorithm,
                digestAlgorithm
        );
    }

    private Function<OutboundResponseFromIdp, String> getTransformer() {
        return stubTransformersFactory.getOutboundResponseFromIdpToStringTransformer(
                encryptionKeyStore,
                keyStore,
                entityToEncryptForLocator,
                signatureAlgorithm,
                digestAlgorithm
        );
    }

    public Function<OutboundResponseFromIdp, String> get(Idp idp) {
        if (publicSigningKey.isPresent() && idp.shouldSendKeyInfo()) {
            return getTransformerWithKeyInfo(idp.getIssuerId());
        } else {
            return getTransformer();
        }
    }
}
