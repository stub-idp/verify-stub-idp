package uk.gov.ida.rp.testrp;

import freemarker.template.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewRenderer;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.domain.request.AuthnRequestFromTransaction;
import stubidp.saml.hub.api.HubTransformersFactory;
import stubidp.saml.hub.metadata.IdpMetadataPublicKeyStore;
import stubidp.saml.hub.transformers.inbound.PassthroughAssertionUnmarshaller;
import stubidp.saml.hub.transformers.inbound.TransactionIdaStatusUnmarshaller;
import stubidp.saml.hub.transformers.outbound.RequestAbstractTypeToStringTransformer;
import stubidp.saml.hub.validators.response.common.ResponseSizeValidator;
import stubidp.saml.metadata.TrustStoreConfiguration;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.DecrypterFactory;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.SamlMessageSignatureValidator;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.security.SignatureValidator;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.core.transformers.AuthnContextFactory;
import stubidp.utils.rest.jerseyclient.DefaultClientProvider;
import stubidp.utils.rest.restclient.RestfulClientConfiguration;
import stubidp.utils.security.security.PublicKeyFileInputStreamFactory;
import stubidp.utils.security.security.PublicKeyInputStreamFactory;
import uk.gov.ida.rp.testrp.authentication.SimpleAuthenticator;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.rp.testrp.controllogic.AuthnResponseReceiverHandler;
import uk.gov.ida.rp.testrp.controllogic.MatchingServiceRequestHandler;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetailsFactory;
import uk.gov.ida.rp.testrp.metadata.MetadataResolverProvider;
import uk.gov.ida.rp.testrp.metadata.SpMetadataPublicKeyStore;
import uk.gov.ida.rp.testrp.providers.KeyStoreProvider;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;
import uk.gov.ida.rp.testrp.saml.locators.TransactionHardCodedEntityToEncryptForLocator;
import uk.gov.ida.rp.testrp.saml.transformers.IdaAuthnRequestFromTransactionToAuthnRequestTransformer;
import uk.gov.ida.rp.testrp.saml.transformers.InboundResponseFromHubUnmarshaller;
import uk.gov.ida.rp.testrp.saml.transformers.SamlResponseToIdaResponseTransformer;
import uk.gov.ida.rp.testrp.saml.validators.NoOpStringSizeValidator;
import uk.gov.ida.rp.testrp.tokenservice.TokenService;
import uk.gov.ida.rp.testrp.views.NonCachingFreemarkerViewRenderer;
import uk.gov.ida.rp.testrp.views.SamlAuthnRequestRedirectViewFactory;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collections;
import java.util.function.Function;

public class TestRpBinder extends AbstractBinder {
    private static final Logger LOG = LoggerFactory.getLogger(TestRpBinder.class);
    private static final String PRIVATE_BETA_ACCESS_RESTRICTION = "Private Beta (token) access restriction";
    private static final SignatureAlgorithm signatureAlgorithm = new SignatureRSASHA256();
    private static final DigestAlgorithm digestAlgorithm = new DigestSHA256();

    private final HubTransformersFactory hubTransformersFactory = new HubTransformersFactory();
    private final CoreTransformersFactory coreTransformersFactory = new CoreTransformersFactory();
    private final TestRpConfiguration configuration;
    private final Environment environment;
    private final MetadataResolver metadataResolver;

    public TestRpBinder(TestRpConfiguration configuration, Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
        this.metadataResolver = new MetadataResolverProvider(new DefaultClientProvider(environment, configuration).get(), configuration).get();
    }

    @Override
    public void configure() {
        bind(KeyStoreProvider.class).to(java.security.KeyStore.class);
        bind(PublicKeyFileInputStreamFactory.class).to(PublicKeyInputStreamFactory.class);
        bind(SimpleAuthenticator.class).to(SimpleAuthenticator.class);

        bind(DefaultClientProvider.class).to(Client.class);

        bind(TransactionHardCodedEntityToEncryptForLocator.class).to(EntityToEncryptForLocator.class);

        bind(SessionRepository.class).to(SessionRepository.class).in(Singleton.class);

        bind(TokenService.class).to(TokenService.class);
        bind(metadataResolver).to(MetadataResolver.class);

        bind(AuthnRequestSenderHandler.class).to(AuthnRequestSenderHandler.class);
        bind(SamlAuthnRequestRedirectViewFactory.class).to(SamlAuthnRequestRedirectViewFactory.class);
        bind(PageErrorMessageDetailsFactory.class).to(PageErrorMessageDetailsFactory.class);
        bind(MatchingServiceRequestHandler.class).to(MatchingServiceRequestHandler.class);
        bind(AuthnResponseReceiverHandler.class).to(AuthnResponseReceiverHandler.class);

        KeyPair encryptionKeyPair = new KeyPair(configuration.getPublicEncryptionCert().getPublicKey(), configuration.getPrivateEncryptionKeyConfiguration().getPrivateKey());
        PublicKey publicKey = configuration.getPublicSigningCert().getPublicKey();
        KeyPair signingKeyPair = new KeyPair(publicKey, configuration.getPrivateSigningKeyConfiguration().getPrivateKey());
        IdaKeyStore idaKeyStore = new IdaKeyStore(signingKeyPair, Collections.singletonList(encryptionKeyPair));
        bind(idaKeyStore).to(IdaKeyStore.class);

        final IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(idaKeyStore);
        bind(keyStoreCredentialRetriever).to(IdaKeyStoreCredentialRetriever.class);

        bind(new SignatureFactory(keyStoreCredentialRetriever, signatureAlgorithm, digestAlgorithm)).to(SignatureFactory.class);

        final String expectedEntityId = configuration.getMsaEntityId();
        bind(expectedEntityId).to(String.class).named("expectedEntityId");

        bind(configuration.getHubEntityId()).to(String.class).named("HubEntityId");
        bind(signatureAlgorithm).to(SignatureAlgorithm.class);
        bind(digestAlgorithm).to(DigestAlgorithm.class);

        bind(configuration.getClientTrustStoreConfiguration()).to(TrustStoreConfiguration.class);
        bind(configuration).to(RestfulClientConfiguration.class);
        bind(environment).to(Environment.class);

        RequestAbstractTypeToStringTransformer<AuthnRequest> requestAbstractTypeToStringTransformer = hubTransformersFactory.getRequestAbstractTypeToStringTransformer(false, idaKeyStore, signatureAlgorithm, digestAlgorithm);
        IdaAuthnRequestFromTransactionToAuthnRequestTransformer authenRequestFromTransactionTransformer = new IdaAuthnRequestFromTransactionToAuthnRequestTransformer(new OpenSamlXmlObjectFactory());

        bind(requestAbstractTypeToStringTransformer.compose(authenRequestFromTransactionTransformer)).to(new GenericType<Function<AuthnRequestFromTransaction, String>>() {});

        final Function<Response, InboundResponseFromHub> samlResponseToIdaResponseTransformer = getSamlResponseToIdaResponseTransformer(configuration, metadataResolver, idaKeyStore);
        if(configuration.isHubExpectedToSignAuthnResponse()) {
            bind(samlResponseToIdaResponseTransformer.compose(hubTransformersFactory.getStringToResponseTransformer())).to(new GenericType<Function<String, InboundResponseFromHub>>() {});
        } else {
            bind(samlResponseToIdaResponseTransformer.compose(hubTransformersFactory.getStringToResponseTransformer(new ResponseSizeValidator(new NoOpStringSizeValidator())))).to(new GenericType<Function<String, InboundResponseFromHub>>() {});
        }

        bind(MatchingServiceRequestHandler.class).to(MatchingServiceRequestHandler.class);
        bind(PageErrorMessageDetailsFactory.class).to(PageErrorMessageDetailsFactory.class);

        bind(180).to(Integer.class).named("sessionCacheTimeoutInMinutes");

        if (configuration.getDontCacheFreemarkerTemplates()) {
            bind(new NonCachingFreemarkerViewRenderer()).to(ViewRenderer.class);
        } else {
            bind(new FreemarkerViewRenderer(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)).to(ViewRenderer.class);
        }
    }

    public MetadataResolver getMetadataResolver() {
        return metadataResolver;
    }

    private SamlResponseToIdaResponseTransformer getSamlResponseToIdaResponseTransformer(
            TestRpConfiguration configuration,
            MetadataResolver metadataResolver,
            IdaKeyStore keyStore) {
        InboundResponseFromHubUnmarshaller inboundResponseFromHubUnmarshaller = new InboundResponseFromHubUnmarshaller(
                new TransactionIdaStatusUnmarshaller(),
                new PassthroughAssertionUnmarshaller(
                        new XmlObjectToBase64EncodedStringTransformer<>(),
                        new AuthnContextFactory()
                )
        );
        IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        return new SamlResponseToIdaResponseTransformer(
                inboundResponseFromHubUnmarshaller,
                new SamlResponseSignatureValidator(getHubMessageSignatureValidator(metadataResolver)),
                new AssertionDecrypter(
                        new EncryptionAlgorithmValidator(),
                        new DecrypterFactory().createDecrypter(idaKeyStoreCredentialRetriever.getDecryptingCredentials())
                ),
                new SamlAssertionsSignatureValidator(getMsaMessageSignatureValidator(metadataResolver)),
                configuration.isHubExpectedToSignAuthnResponse()
        );
    }

    private SamlMessageSignatureValidator getHubMessageSignatureValidator(MetadataResolver metadataResolver) {
        final SignatureValidator hubSignatureValidator = coreTransformersFactory.getSignatureValidator(new SpMetadataPublicKeyStore(metadataResolver));
        return new SamlMessageSignatureValidator(hubSignatureValidator);
    }

    private SamlMessageSignatureValidator getMsaMessageSignatureValidator(MetadataResolver metadataResolver) {
        final SignatureValidator msaSignatureValidator = coreTransformersFactory.getSignatureValidator(new IdpMetadataPublicKeyStore(metadataResolver));
        return new SamlMessageSignatureValidator(msaSignatureValidator);
    }
}
