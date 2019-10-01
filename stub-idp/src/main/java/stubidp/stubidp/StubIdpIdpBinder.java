package stubidp.stubidp;

import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.metadata.MetadataConfiguration;
import stubidp.saml.metadata.MetadataHealthCheck;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import stubidp.saml.metadata.factories.DropwizardMetadataResolverFactory;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.SigningKeyStore;
import stubidp.saml.stubidp.configuration.SamlConfiguration;
import stubidp.saml.stubidp.stub.transformers.inbound.AuthnRequestToIdaRequestFromHubTransformer;
import stubidp.stubidp.configuration.AssertionLifetimeConfiguration;
import stubidp.stubidp.configuration.SigningKeyPairConfiguration;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.domain.factories.AssertionFactory;
import stubidp.stubidp.domain.factories.AssertionRestrictionsFactory;
import stubidp.stubidp.domain.factories.IdentityProviderAssertionFactory;
import stubidp.stubidp.domain.factories.StubTransformersFactory;
import stubidp.stubidp.repositories.MetadataRepository;
import stubidp.stubidp.saml.IdpAuthnRequestValidator;
import stubidp.stubidp.saml.locators.IdpHardCodedEntityToEncryptForLocator;
import stubidp.stubidp.saml.transformers.OutboundResponseFromIdpTransformerProvider;
import stubidp.stubidp.security.HubEncryptionKeyStore;
import stubidp.stubidp.security.IdaAuthnRequestKeyStore;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.services.SuccessAuthnResponseService;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StubIdpIdpBinder extends AbstractBinder {

    public static final String HUB_METADATA_REPOSITORY = "HubMetadataRepository";
    public static final String HUB_METADATA_RESOLVER = "HubMetadataResolver";
    public static final String HUB_ENTITY_ID = "HubEntityId";
    public static final String HUB_METADATA_CONFIGURATION = "HubMetadataConfiguration";

    private final StubIdpConfiguration stubIdpConfiguration;
    private final Environment environment;

    StubIdpIdpBinder(StubIdpConfiguration stubIdpConfiguration,
                     Environment environment) {
        this.stubIdpConfiguration = stubIdpConfiguration;
        this.environment = environment;
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

        final MetadataResolver idpMetadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolver(environment, stubIdpConfiguration.getMetadataConfiguration());
        registerMetadataHealthcheckAndRefresh(environment, idpMetadataResolver, stubIdpConfiguration.getMetadataConfiguration(), "metadata");
        bind(idpMetadataResolver).named(HUB_METADATA_RESOLVER).to(MetadataResolver.class);
        final MetadataRepository idpMetadataRepository = new MetadataRepository(idpMetadataResolver, hubEntityId);
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
        final StubTransformersFactory stubTransformersFactory = new StubTransformersFactory();
        bind(stubTransformersFactory.getAuthnRequestToIdaRequestFromHubTransformer(signingKeyStore)).to(AuthnRequestToIdaRequestFromHubTransformer.class);
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
    }

    private void registerMetadataHealthcheckAndRefresh(Environment environment, MetadataResolver metadataResolver, MetadataResolverConfiguration metadataResolverConfiguration, String name) {
        String expectedEntityId = metadataResolverConfiguration.getExpectedEntityId();
        MetadataHealthCheck metadataHealthCheck = new MetadataHealthCheck(metadataResolver, expectedEntityId);
        environment.healthChecks().register(name, metadataHealthCheck);

        environment.admin().addTask(new Task(name + "-refresh") {
            @Override
            public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
                ((AbstractReloadingMetadataResolver) metadataResolver).refresh();
            }
        });
    }

    private IdaKeyStore getKeystoreFromConfig(SigningKeyPairConfiguration keyPairConfiguration) {
        PrivateKey privateSigningKey = keyPairConfiguration.getPrivateKey();
        X509Certificate signingCertificate = new X509CertificateFactory().createCertificate(keyPairConfiguration.getCert());
        PublicKey publicSigningKey = signingCertificate.getPublicKey();
        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);

        return new IdaKeyStore(signingCertificate, signingKeyPair, Collections.emptyList());
    }
}
