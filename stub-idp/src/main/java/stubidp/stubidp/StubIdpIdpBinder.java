package stubidp.stubidp;

import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.metadata.MetadataConfiguration;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.security.SigningKeyStore;
import stubidp.saml.stubidp.configuration.SamlConfiguration;
import stubidp.saml.stubidp.stub.transformers.inbound.AuthnRequestToIdaRequestFromHubTransformer;
import stubidp.shared.configuration.SigningKeyPairConfiguration;
import stubidp.shared.repositories.MetadataRepository;
import stubidp.stubidp.builders.IdpMetadataBuilder;
import stubidp.stubidp.configuration.AssertionLifetimeConfiguration;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.domain.factories.AssertionFactory;
import stubidp.stubidp.domain.factories.AssertionRestrictionsFactory;
import stubidp.stubidp.domain.factories.IdentityProviderAssertionFactory;
import stubidp.stubidp.domain.factories.StubTransformersFactory;
import stubidp.stubidp.saml.IdpAuthnRequestValidator;
import stubidp.stubidp.saml.locators.IdpHardCodedEntityToEncryptForLocator;
import stubidp.stubidp.saml.transformers.OutboundResponseFromIdpTransformerProvider;
import stubidp.stubidp.security.HubEncryptionKeyStore;
import stubidp.stubidp.security.IdaAuthnRequestKeyStore;
import stubidp.stubidp.services.GeneratePasswordService;
import stubidp.stubidp.services.IdpUserService;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.services.SuccessAuthnResponseService;
import stubidp.stubidp.services.UserService;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Optional;

public class StubIdpIdpBinder extends AbstractBinder {

    public static final String HUB_METADATA_REPOSITORY = "HubMetadataRepository";
    public static final String HUB_METADATA_RESOLVER = "HubMetadataResolver";
    public static final String HUB_ENTITY_ID = "HubEntityId";
    public static final String HUB_METADATA_CONFIGURATION = "HubMetadataConfiguration";
    public static final String IDP_METADATA_SIGNING_KEYSTORE = "IdpMetadataSigningKeystore";
    public static final String IDP_METADATA_SIGNATURE_FACTORY = "IdpMetadataSignatureFactory";
    public static final String IDP_SIGNING_CERT = "IdpSigningCert";

    private final StubIdpConfiguration stubIdpConfiguration;
    private final Environment environment;
    private final MetadataResolverBundle<StubIdpConfiguration> idpMetadataResolverBundle;

    StubIdpIdpBinder(StubIdpConfiguration stubIdpConfiguration,
                     Environment environment,
                     MetadataResolverBundle<StubIdpConfiguration> idpMetadataResolverBundle) {
        this.stubIdpConfiguration = stubIdpConfiguration;
        this.environment = environment;
        this.idpMetadataResolverBundle = idpMetadataResolverBundle;
    }

    @Override
    protected void configure() {
        bind(stubIdpConfiguration).to(AssertionLifetimeConfiguration.class);
        bind(stubIdpConfiguration.getSamlConfiguration()).to(SamlConfiguration.class);
        final String hubEntityId = stubIdpConfiguration.getHubEntityId();
        bind(hubEntityId).named(HUB_ENTITY_ID).to(String.class);

        final IdpHardCodedEntityToEncryptForLocator entityToEncryptForLocator = new IdpHardCodedEntityToEncryptForLocator(hubEntityId);
        bind(entityToEncryptForLocator).to(EntityToEncryptForLocator.class);
        bind(PublicKeyFactory.class).to(PublicKeyFactory.class);

        final MetadataResolver idpMetadataResolver = idpMetadataResolverBundle.getMetadataResolver();
        bind(idpMetadataResolver).named(HUB_METADATA_RESOLVER).to(MetadataResolver.class);
        final MetadataRepository idpMetadataRepository = new MetadataRepository(idpMetadataResolverBundle.getMetadataCredentialResolver(), hubEntityId);
        bind(idpMetadataRepository).named(HUB_METADATA_REPOSITORY).to(MetadataRepository.class);
        final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        bind(stubIdpConfiguration.getMetadataConfiguration()).named(HUB_METADATA_CONFIGURATION).to(MetadataConfiguration.class);

        final IdaAuthnRequestKeyStore signingKeyStore = new IdaAuthnRequestKeyStore(idpMetadataRepository, publicKeyFactory);
        bind(signingKeyStore).to(SigningKeyStore.class);

        bind(AssertionFactory.class).to(AssertionFactory.class);
        bind(AssertionRestrictionsFactory.class).to(AssertionRestrictionsFactory.class);
        bind(IdentityProviderAssertionFactory.class).to(IdentityProviderAssertionFactory.class);
        final SignatureRSASHA256 signatureAlgorithm = new SignatureRSASHA256();
        bind(signatureAlgorithm).to(SignatureAlgorithm.class);
        final DigestSHA256 digestAlgorithm = new DigestSHA256();
        bind(digestAlgorithm).to(DigestAlgorithm.class);

        bind(IdpMetadataBuilder.class).to(IdpMetadataBuilder.class);
        final IdaKeyStore idpMetadataSigningKeyStore = getKeystoreFromConfig(stubIdpConfiguration.getIdpMetadataSigningKeyPairConfiguration());
        bind(idpMetadataSigningKeyStore).named(IDP_METADATA_SIGNING_KEYSTORE).to(IdaKeyStore.class);
        final SignatureFactory signatureFactory = new SignatureFactory(true, new IdaKeyStoreCredentialRetriever(idpMetadataSigningKeyStore), signatureAlgorithm, digestAlgorithm);
        bind(signatureFactory).named(IDP_METADATA_SIGNATURE_FACTORY).to(SignatureFactory.class);

        final StubTransformersFactory stubTransformersFactory = new StubTransformersFactory();
        bind(stubTransformersFactory.getAuthnRequestToIdaRequestFromHubTransformer(signingKeyStore)).to(AuthnRequestToIdaRequestFromHubTransformer.class);
        bind(stubIdpConfiguration.getSigningKeyPairConfiguration().getCert()).named(IDP_SIGNING_CERT).to(String.class);
        final IdaKeyStore idpSigningKeyStore = getKeystoreFromConfig(stubIdpConfiguration.getSigningKeyPairConfiguration());
        final HubEncryptionKeyStore hubEncryptionKeyStore = new HubEncryptionKeyStore(idpMetadataRepository, publicKeyFactory);
        bind(new OutboundResponseFromIdpTransformerProvider(hubEncryptionKeyStore,
                idpSigningKeyStore, entityToEncryptForLocator,
                Optional.ofNullable(stubIdpConfiguration.getSigningKeyPairConfiguration().getCert()),
                stubTransformersFactory, signatureAlgorithm, digestAlgorithm)).to(OutboundResponseFromIdpTransformerProvider.class);

        bind(IdpAuthnRequestValidator.class).to(IdpAuthnRequestValidator.class);

        // bound elsewhere as used by idp + eidas
//        bind(AuthnRequestReceiverService.class).to(AuthnRequestReceiverService.class);
        bind(SuccessAuthnResponseService.class).to(SuccessAuthnResponseService.class);
        bind(NonSuccessAuthnResponseService.class).to(NonSuccessAuthnResponseService.class);

        // user resource + password gen
        bind(GeneratePasswordService.class).to(GeneratePasswordService.class);
        bind(IdpUserService.class).to(IdpUserService.class);
        bind(UserService.class).to(UserService.class);
    }

    private IdaKeyStore getKeystoreFromConfig(SigningKeyPairConfiguration keyPairConfiguration) {
        PrivateKey privateSigningKey = keyPairConfiguration.getPrivateKey();
        X509Certificate signingCertificate = new X509CertificateFactory().createCertificate(keyPairConfiguration.getCert());
        PublicKey publicSigningKey = signingCertificate.getPublicKey();
        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);

        return new IdaKeyStore(signingCertificate, signingKeyPair, Collections.emptyList());
    }
}
