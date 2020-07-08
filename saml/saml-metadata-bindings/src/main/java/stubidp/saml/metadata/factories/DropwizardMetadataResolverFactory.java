package stubidp.saml.metadata.factories;

import io.dropwizard.setup.Environment;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import stubidp.saml.metadata.CertificateChainValidationFilter;
import stubidp.saml.metadata.ExpiredCertificateMetadataFilter;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import stubidp.saml.metadata.PKIXSignatureValidationFilterProvider;
import stubidp.utils.security.security.X509CertificateFactory;
import stubidp.utils.security.security.verification.CertificateChainValidator;
import stubidp.utils.security.security.verification.PKIXParametersProvider;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.security.KeyStore;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class DropwizardMetadataResolverFactory {
    private final MetadataResolverFactory metadataResolverFactory = new MetadataResolverFactory();
    private final ExpiredCertificateMetadataFilter expiredCertificateMetadataFilter = new ExpiredCertificateMetadataFilter();
    private final MetadataClientFactory metadataClientFactory = new MetadataClientFactory();
    private final CertificateChainValidator certificateChainValidator = new CertificateChainValidator(new PKIXParametersProvider(), new X509CertificateFactory());

    public MetadataResolver createMetadataResolver(
            Environment environment,
            MetadataResolverConfiguration metadataConfiguration) {
        return createMetadataResolver(environment, metadataConfiguration, true);
    }

    public MetadataResolver createMetadataResolverWithoutSignatureValidation(
            Environment environment,
            MetadataResolverConfiguration metadataConfiguration) {
        return createMetadataResolver(environment, metadataConfiguration, false);
    }

    public MetadataResolver createMetadataResolver(
            Environment environment,
            MetadataResolverConfiguration metadataConfiguration,
            boolean validateSignatures) {
        return createMetadataResolverWithClient(metadataConfiguration, validateSignatures, metadataClientFactory.getClient(environment, metadataConfiguration));
    }

    public MetadataResolver createMetadataResolverWithClient(
            MetadataResolverConfiguration metadataConfiguration,
            boolean validateSignatures,
            Client client) {
        URI uri = metadataConfiguration.getUri();
        Duration minRefreshDelay = metadataConfiguration.getMinRefreshDelay();
        Duration maxRefreshDelay = metadataConfiguration.getMaxRefreshDelay();

        return metadataResolverFactory.create(
            client,
            uri,
            getMetadataFilters(
                metadataConfiguration,
                validateSignatures,
                metadataConfiguration.getSpTrustStore(),
                metadataConfiguration.getIdpTrustStore()),
            minRefreshDelay,
            maxRefreshDelay
        );
    }

    private List<MetadataFilter> getMetadataFilters(
        final MetadataResolverConfiguration metadataConfiguration,
        final boolean validateSignatures,
        final Optional<KeyStore> spTrustStore,
        final Optional<KeyStore> idpTrustStore) {

        if (!validateSignatures) { return emptyList(); }

        KeyStore metadataTrustStore = metadataConfiguration.getTrustStore();
        PKIXSignatureValidationFilterProvider pkixSignatureValidationFilterProvider = new PKIXSignatureValidationFilterProvider(metadataTrustStore);

        ArrayList<MetadataFilter> metadataFilters = new ArrayList<>();
        metadataFilters.add(pkixSignatureValidationFilterProvider.get());
        metadataFilters.add(expiredCertificateMetadataFilter);

        spTrustStore.ifPresent(
            hubKeyStore ->
                metadataFilters.add(new CertificateChainValidationFilter(
                    SPSSODescriptor.DEFAULT_ELEMENT_NAME,
                    certificateChainValidator,
                    hubKeyStore)));

        idpTrustStore.ifPresent(
            idpKeyStore ->
                metadataFilters.add(new CertificateChainValidationFilter(
                    IDPSSODescriptor.DEFAULT_ELEMENT_NAME,
                    certificateChainValidator,
                    idpKeyStore)));

        return List.copyOf(metadataFilters);
    }
}
