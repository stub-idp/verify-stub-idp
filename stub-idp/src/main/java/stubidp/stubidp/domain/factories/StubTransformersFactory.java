package stubidp.stubidp.domain.factories;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.core.transformers.outbound.ResponseToSignedStringTransformer;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.utils.hub.domain.IdaAuthnRequestFromHub;
import stubidp.saml.utils.hub.factories.AttributeFactory_1_1;
import stubidp.saml.utils.hub.transformers.inbound.decorators.AuthnRequestSizeValidator;
import stubidp.saml.utils.hub.validators.StringSizeValidator;
import stubidp.saml.stubidp.stub.transformers.inbound.AuthnRequestToIdaRequestFromHubTransformer;
import stubidp.saml.stubidp.stub.transformers.inbound.IdaAuthnRequestFromHubUnmarshaller;
import stubidp.saml.stubidp.stub.transformers.outbound.IdentityProviderAssertionToAssertionTransformer;
import stubidp.saml.stubidp.stub.transformers.outbound.IdentityProviderAuthnStatementToAuthnStatementTransformer;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.SigningKeyStore;
import stubidp.stubidp.domain.IdpIdaStatusMarshaller;
import stubidp.stubidp.domain.OutboundResponseFromIdp;
import stubidp.stubidp.saml.transformers.OutboundResponseFromIdpToSamlResponseTransformer;

import java.util.function.Function;

public class StubTransformersFactory {

    private CoreTransformersFactory coreTransformersFactory;


    public StubTransformersFactory() {
        this.coreTransformersFactory = new CoreTransformersFactory();
    }

    public Function<String, IdaAuthnRequestFromHub> getStringToIdaAuthnRequestFromHub(
            final SigningKeyStore signingKeyStore){
        AuthnRequestSizeValidator sizeValidator = new AuthnRequestSizeValidator(new StringSizeValidator());


        StringToOpenSamlObjectTransformer<AuthnRequest> stringtoOpenSamlObjectTransformer = coreTransformersFactory.<AuthnRequest>getStringtoOpenSamlObjectTransformer(sizeValidator);

        return getAuthnRequestToIdaRequestFromHubTransformer(signingKeyStore).compose(stringtoOpenSamlObjectTransformer);
    }

    public Function<String, AuthnRequest> getStringToAuthnRequest(){
        AuthnRequestSizeValidator sizeValidator = new AuthnRequestSizeValidator(new StringSizeValidator());

        StringToOpenSamlObjectTransformer<AuthnRequest> stringtoOpenSamlObjectTransformer = coreTransformersFactory.<AuthnRequest>getStringtoOpenSamlObjectTransformer(sizeValidator);

        return stringtoOpenSamlObjectTransformer;
    }

    public AuthnRequestToIdaRequestFromHubTransformer getAuthnRequestToIdaRequestFromHubTransformer(SigningKeyStore signingKeyStore) {
        return new AuthnRequestToIdaRequestFromHubTransformer(
                new IdaAuthnRequestFromHubUnmarshaller(),
                coreTransformersFactory.<AuthnRequest>getSamlRequestSignatureValidator(signingKeyStore)
        );
    }

    public Function<OutboundResponseFromIdp, String> getOutboundResponseFromIdpToStringTransformer(
            final EncryptionKeyStore publicKeyStore,
            final IdaKeyStore keyStore,
            EntityToEncryptForLocator entityToEncryptForLocator,
            String publicSigningKey,
            String issuerId,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm
    ){

        return coreTransformersFactory.getResponseStringTransformer(
                publicKeyStore,
                keyStore,
                entityToEncryptForLocator,
                publicSigningKey,
                issuerId,
                signatureAlgorithm,
                digestAlgorithm).compose(getOutboundResponseFromIdpToSamlResponseTransformer());
    }

    public Function<OutboundResponseFromIdp, String> getOutboundResponseFromIdpToStringTransformer(
            final EncryptionKeyStore encryptionKeyStore,
            final IdaKeyStore keyStore,
            EntityToEncryptForLocator entityToEncryptForLocator,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm
    ){
        ResponseToSignedStringTransformer responseStringTransformer = coreTransformersFactory.getResponseStringTransformer(
                encryptionKeyStore,
                keyStore,
                entityToEncryptForLocator,
                signatureAlgorithm,
                digestAlgorithm);

        return responseStringTransformer.compose(getOutboundResponseFromIdpToSamlResponseTransformer());
    }

    public OutboundResponseFromIdpToSamlResponseTransformer getOutboundResponseFromIdpToSamlResponseTransformer() {
        OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        return new OutboundResponseFromIdpToSamlResponseTransformer(
                new IdpIdaStatusMarshaller(openSamlXmlObjectFactory),
                openSamlXmlObjectFactory,
                getIdpAssertionToAssertionTransformer()
        );
    }

    private IdentityProviderAssertionToAssertionTransformer getIdpAssertionToAssertionTransformer() {
        OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        return new IdentityProviderAssertionToAssertionTransformer(
                openSamlXmlObjectFactory,
                new AttributeFactory_1_1(openSamlXmlObjectFactory),
                new IdentityProviderAuthnStatementToAuthnStatementTransformer(openSamlXmlObjectFactory),
                new OutboundAssertionToSubjectTransformer(openSamlXmlObjectFactory)
        );
    }

}
