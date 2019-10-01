package stubidp.saml.utils.core.api;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import stubidp.saml.security.CredentialFactorySignatureValidator;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.KeyStoreBackedEncryptionCredentialResolver;
import stubidp.saml.security.SamlMessageSignatureValidator;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.security.SignatureValidator;
import stubidp.saml.security.SignatureWithKeyInfoFactory;
import stubidp.saml.security.SigningCredentialFactory;
import stubidp.saml.security.SigningKeyStore;
import stubidp.saml.security.validators.signature.SamlRequestSignatureValidator;
import stubidp.saml.serializers.deserializers.ElementToOpenSamlXMLObjectTransformer;
import stubidp.saml.serializers.deserializers.OpenSamlXMLObjectUnmarshaller;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.serializers.deserializers.parser.SamlObjectParser;
import stubidp.saml.serializers.deserializers.validators.Base64StringDecoder;
import stubidp.saml.serializers.deserializers.validators.NotNullSamlStringValidator;
import stubidp.saml.serializers.deserializers.validators.SizeValidator;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.transformers.inbound.Cycle3DatasetFactory;
import stubidp.saml.utils.core.transformers.inbound.HubAssertionUnmarshaller;
import stubidp.saml.utils.core.transformers.outbound.ResponseToSignedStringTransformer;
import stubidp.saml.utils.core.transformers.outbound.UnsignedAssertionResponseToSignedStringTransformer;
import stubidp.saml.utils.core.transformers.outbound.decorators.ResponseAssertionSigner;
import stubidp.saml.utils.core.transformers.outbound.decorators.ResponseSignatureCreator;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlResponseAssertionEncrypter;
import stubidp.saml.utils.core.transformers.outbound.decorators.SamlSignatureSigner;
import stubidp.saml.utils.metadata.transformers.KeyDescriptorsUnmarshaller;

public class CoreTransformersFactory {
    public KeyDescriptorsUnmarshaller getCertificatesToKeyDescriptorsTransformer() {
        return new KeyDescriptorsUnmarshaller(
                new OpenSamlXmlObjectFactory()
        );
    }

    public <T extends XMLObject> XmlObjectToElementTransformer<T> getXmlObjectToElementTransformer() {
        return new XmlObjectToElementTransformer<>();
    }

    public <T extends XMLObject> ElementToOpenSamlXMLObjectTransformer<T> getElementToOpenSamlXmlObjectTransformer() {
        return new ElementToOpenSamlXMLObjectTransformer<>(
                new SamlObjectParser()
        );
    }

    public HubAssertionUnmarshaller getAssertionToHubAssertionTransformer(String hubEntityId) {
        return new HubAssertionUnmarshaller(
                new Cycle3DatasetFactory(),
                hubEntityId
        );
    }

    public <TOutput extends XMLObject> StringToOpenSamlObjectTransformer<TOutput> getStringtoOpenSamlObjectTransformer(
            final SizeValidator sizeValidator
    ) {
        return new StringToOpenSamlObjectTransformer<>(
                new NotNullSamlStringValidator(),
                new Base64StringDecoder(),
                sizeValidator,
                new OpenSamlXMLObjectUnmarshaller<TOutput>(new SamlObjectParser())
        );
    }

    public <TInput extends RequestAbstractType> SamlRequestSignatureValidator<TInput> getSamlRequestSignatureValidator(
            final SigningKeyStore publicKeyStore
    ) {
        return new SamlRequestSignatureValidator<>(
                new SamlMessageSignatureValidator(getSignatureValidator(publicKeyStore))
        );
    }

    public SignatureValidator getSignatureValidator(SigningKeyStore signingKeyStore) {
        SigningCredentialFactory signingCredentialFactory = new SigningCredentialFactory(signingKeyStore);
        return getSignatureValidator(signingCredentialFactory);
    }

    public SignatureValidator getSignatureValidator(SigningCredentialFactory publicCredentialFactory) {
        return new CredentialFactorySignatureValidator(publicCredentialFactory);
    }

    public ResponseToSignedStringTransformer getResponseStringTransformer(
            final EncryptionKeyStore publicKeyStore,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final SignatureAlgorithm signatureAlgorithm,
            final DigestAlgorithm digestAlgorithm) {
        return getResponseStringTransformer(publicKeyStore, keyStore, entityToEncryptForLocator, signatureAlgorithm,
                digestAlgorithm, new EncrypterFactory());
    }

    public ResponseToSignedStringTransformer getResponseStringTransformer(
            final EncryptionKeyStore publicKeyStore,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final SignatureAlgorithm signatureAlgorithm,
            final DigestAlgorithm digestAlgorithm,
            final EncrypterFactory encrypterFactory) {
        SignatureFactory signatureFactory = new SignatureFactory(new IdaKeyStoreCredentialRetriever(keyStore), signatureAlgorithm, digestAlgorithm);
        ResponseAssertionSigner responseAssertionSigner = new ResponseAssertionSigner(signatureFactory);
        return getResponseStringTransformer(publicKeyStore, entityToEncryptForLocator, encrypterFactory, signatureFactory, responseAssertionSigner);
    }

    public ResponseToSignedStringTransformer getResponseStringTransformer(
            final EncryptionKeyStore encryptionKeyStore,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final String publicSigningKey,
            final String issuerId,
            final SignatureAlgorithm signatureAlgorithm,
            final DigestAlgorithm digestAlgorithm
    ) {
        SignatureFactory signatureFactory = new SignatureWithKeyInfoFactory(new IdaKeyStoreCredentialRetriever(keyStore), signatureAlgorithm, digestAlgorithm, issuerId, publicSigningKey);
        ResponseAssertionSigner responseAssertionSigner = new ResponseAssertionSigner(signatureFactory);
        return getResponseStringTransformer(encryptionKeyStore, entityToEncryptForLocator, new EncrypterFactory(), signatureFactory, responseAssertionSigner);
    }

    public ResponseToSignedStringTransformer getResponseStringTransformer(
            final EncryptionKeyStore publicKeyStore,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final ResponseAssertionSigner responseAssertionSigner,
            final SignatureAlgorithm signatureAlgorithm,
            final DigestAlgorithm digestAlgorithm) {
        SignatureFactory signatureFactory = new SignatureFactory(new IdaKeyStoreCredentialRetriever(keyStore), signatureAlgorithm, digestAlgorithm);
        return getResponseStringTransformer(publicKeyStore, entityToEncryptForLocator, new EncrypterFactory(), signatureFactory, responseAssertionSigner);
    }

    private ResponseToSignedStringTransformer getResponseStringTransformer(
            final EncryptionKeyStore publicKeyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final EncrypterFactory encrypterFactory,
            final SignatureFactory signatureFactory,
            final ResponseAssertionSigner responseAssertionSigner) {
        SamlResponseAssertionEncrypter responseAssertionEncrypter =
                new SamlResponseAssertionEncrypter(
                        new KeyStoreBackedEncryptionCredentialResolver(publicKeyStore),
                        encrypterFactory,
                        entityToEncryptForLocator);
        return new ResponseToSignedStringTransformer(
                new XmlObjectToBase64EncodedStringTransformer<>(),
                new SamlSignatureSigner<>(),
                responseAssertionEncrypter,
                responseAssertionSigner,
                new ResponseSignatureCreator(signatureFactory)
        );
    }

    public UnsignedAssertionResponseToSignedStringTransformer getUnsignedAssertionResponseStringTransformer(
            final EncryptionKeyStore publicKeyStore,
            final IdaKeyStore keyStore,
            final EntityToEncryptForLocator entityToEncryptForLocator,
            final SignatureAlgorithm signatureAlgorithm,
            final DigestAlgorithm digestAlgorithm,
            final EncrypterFactory encrypterFactory) {

        SignatureFactory signatureFactory = new SignatureFactory(
                new IdaKeyStoreCredentialRetriever(keyStore),
                signatureAlgorithm,
                digestAlgorithm);

        SamlResponseAssertionEncrypter responseAssertionEncrypter =
                new SamlResponseAssertionEncrypter(
                        new KeyStoreBackedEncryptionCredentialResolver(publicKeyStore),
                        encrypterFactory,
                        entityToEncryptForLocator);

        return new UnsignedAssertionResponseToSignedStringTransformer(
                new XmlObjectToBase64EncodedStringTransformer<>(),
                new SamlSignatureSigner<>(),
                responseAssertionEncrypter,
                new ResponseSignatureCreator(signatureFactory)
        );
    }
}
