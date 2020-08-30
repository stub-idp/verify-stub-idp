package stubsp.stubsp.saml;

import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import stubidp.saml.domain.response.OutboundResponseFromIdp;
import stubidp.saml.hub.transformers.outbound.IdpIdaStatusMarshaller;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.core.transformers.outbound.ResponseToSignedStringTransformer;
import stubidp.saml.utils.hub.factories.AttributeFactory_1_1;

import java.util.function.Function;

public class IdpSamlGenerator {

    private static final CoreTransformersFactory coreTransformersFactory = new CoreTransformersFactory();
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private final EncryptionKeyStore encryptionKeyStore;
    private final IdaKeyStore keyStore;
    private final EntityToEncryptForLocator entityToEncryptForLocator;
    private final SignatureAlgorithm signatureAlgorithm;
    private final DigestAlgorithm digestAlgorithm;

    public IdpSamlGenerator(EncryptionKeyStore encryptionKeyStore,
                            IdaKeyStore keyStore,
                            EntityToEncryptForLocator entityToEncryptForLocator,
                            SignatureAlgorithm signatureAlgorithm,
                            DigestAlgorithm digestAlgorithm) {

        this.encryptionKeyStore = encryptionKeyStore;
        this.keyStore = keyStore;
        this.entityToEncryptForLocator = entityToEncryptForLocator;
        this.signatureAlgorithm = signatureAlgorithm;
        this.digestAlgorithm = digestAlgorithm;
    }

    public String generate(OutboundResponseFromIdp outboundResponseFromIdp) {
        return getOutboundResponseFromIdpToStringTransformer().apply(outboundResponseFromIdp);
    }

    private Function<OutboundResponseFromIdp, String> getOutboundResponseFromIdpToStringTransformer(){
        ResponseToSignedStringTransformer responseStringTransformer = coreTransformersFactory.getResponseStringTransformer(
                encryptionKeyStore,
                keyStore,
                entityToEncryptForLocator,
                signatureAlgorithm,
                digestAlgorithm);

        return responseStringTransformer.compose(getOutboundResponseFromIdpToSamlResponseTransformer());
    }


    private OutboundResponseFromIdpToSamlResponseTransformer getOutboundResponseFromIdpToSamlResponseTransformer() {
        return new OutboundResponseFromIdpToSamlResponseTransformer(
                new IdpIdaStatusMarshaller(openSamlXmlObjectFactory),
                openSamlXmlObjectFactory,
                getIdpAssertionToAssertionTransformer()
        );
    }

    private IdentityProviderAssertionToAssertionTransformer getIdpAssertionToAssertionTransformer() {
        return new IdentityProviderAssertionToAssertionTransformer(
                openSamlXmlObjectFactory,
                new AttributeFactory_1_1(openSamlXmlObjectFactory),
                new IdentityProviderAuthnStatementToAuthnStatementTransformer(openSamlXmlObjectFactory),
                new OutboundAssertionToSubjectTransformer(openSamlXmlObjectFactory)
        );
    }
}
