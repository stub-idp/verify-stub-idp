package stubidp.saml.metadata.bundle;

import io.dropwizard.Configuration;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import stubidp.saml.metadata.MetadataHealthCheck;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import stubidp.saml.metadata.exception.MetadataResolverCreationException;
import stubidp.saml.metadata.factories.CredentialResolverFactory;
import stubidp.saml.metadata.factories.DropwizardMetadataResolverFactory;
import stubidp.saml.metadata.factories.MetadataSignatureTrustEngineFactory;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MetadataResolverBundle<T extends Configuration> implements io.dropwizard.ConfiguredBundle<T> {
    private final MetadataConfigurationExtractor<T> configExtractor;
    private MetadataResolver metadataResolver;
    private DropwizardMetadataResolverFactory dropwizardMetadataResolverFactory = new DropwizardMetadataResolverFactory();
    private ExplicitKeySignatureTrustEngine signatureTrustEngine;
    private MetadataCredentialResolver credentialResolver;
    private final boolean validateSignatures;
    private final String metadataRefreshName;

    public MetadataResolverBundle(MetadataConfigurationExtractor<T> configExtractor, String metadataRefreshName) {
        this(configExtractor, true, metadataRefreshName);
    }

    public MetadataResolverBundle(MetadataConfigurationExtractor<T> configExtractor) {
        this(configExtractor, true, "metadata");
    }

    public MetadataResolverBundle(MetadataConfigurationExtractor<T> configExtractor, boolean validateSignatures, String metadataRefreshName) {
        this.configExtractor = configExtractor;
        this.validateSignatures = validateSignatures;
        this.metadataRefreshName = metadataRefreshName;
    }

    @Override
    public void run(T configuration, Environment environment) {
        configExtractor.getMetadataConfiguration(configuration).ifPresent(mc -> {
            metadataResolver = dropwizardMetadataResolverFactory.createMetadataResolver(environment, mc, validateSignatures);
            try {
                signatureTrustEngine = new MetadataSignatureTrustEngineFactory().createSignatureTrustEngine(metadataResolver);
                credentialResolver = new CredentialResolverFactory().create(metadataResolver);
            } catch (ComponentInitializationException e) {
                throw new MetadataResolverCreationException(mc.getUri(), e.getMessage());
            }

            MetadataHealthCheck healthCheck = new MetadataHealthCheck(
                    metadataResolver,
                    mc.getExpectedEntityId()
            );

            environment.healthChecks().register(mc.getUri().toString(), healthCheck);
            environment.admin().addTask(new Task(metadataRefreshName+"-refresh") {
                @Override
                public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
                    ((AbstractReloadingMetadataResolver) metadataResolver).refresh();
                }
            });
        });
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        //NOOP
    }

    @Nullable
    public MetadataResolver getMetadataResolver() {
        return metadataResolver;
    }

    public Provider<MetadataResolver> getMetadataResolverProvider() {
        return () -> metadataResolver;
    }

    @Nullable
    public ExplicitKeySignatureTrustEngine getSignatureTrustEngine() {
        return signatureTrustEngine;
    }

    public Provider<ExplicitKeySignatureTrustEngine> getSignatureTrustEngineProvider() {
        return () -> signatureTrustEngine;
    }

    @Nullable
    public MetadataCredentialResolver getMetadataCredentialResolver() {
        return credentialResolver;
    }

    public Provider<MetadataCredentialResolver> getMetadataCredentialResolverProvider() {
        return () -> credentialResolver;
    }

    public interface MetadataConfigurationExtractor<T> {
        Optional<MetadataResolverConfiguration> getMetadataConfiguration(T configuration);
    }

}
