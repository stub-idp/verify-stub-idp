package uk.gov.ida.matchingserviceadapter.repositories;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.AttributeService;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.EntitiesDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.SingleSignOnServiceBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.metadata.transformers.KeyDescriptorsUnmarshaller;
import stubidp.utils.security.security.Certificate;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.CertificateStore;
import uk.gov.ida.matchingserviceadapter.exceptions.FederationMetadataLoadingException;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MatchingServiceAdapterMetadataRepository {

    private final MatchingServiceAdapterConfiguration msaConfiguration;
    private final KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller;
    private final Function<EntitiesDescriptor, Element> entitiesDescriptorElementTransformer;
    private final CertificateStore certificateStore;
    private final MetadataResolver metadataResolver;
    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;
    private final String hubEntityId;
    private final Clock clock;

    @Inject
    public MatchingServiceAdapterMetadataRepository(
            KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller,
            Function<EntitiesDescriptor, Element> entitiesDescriptorElementTransformer,
            CertificateStore certificateStore,
            MetadataResolver metadataResolver,
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            @Named("HubEntityId") String hubEntityId) {
        this(matchingServiceAdapterConfiguration, keyDescriptorsUnmarshaller, entitiesDescriptorElementTransformer, certificateStore, metadataResolver, matchingServiceAdapterConfiguration, hubEntityId, Clock.systemUTC());
    }

    MatchingServiceAdapterMetadataRepository(
            MatchingServiceAdapterConfiguration msaConfiguration,
            KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller,
            Function<EntitiesDescriptor, Element> entitiesDescriptorElementTransformer,
            CertificateStore certificateStore,
            MetadataResolver metadataResolver,
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            @Named("HubEntityId") String hubEntityId,
            Clock clock) {
        this.msaConfiguration = msaConfiguration;
        this.keyDescriptorsUnmarshaller = keyDescriptorsUnmarshaller;
        this.entitiesDescriptorElementTransformer = entitiesDescriptorElementTransformer;
        this.certificateStore = certificateStore;
        this.metadataResolver = metadataResolver;
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
        this.hubEntityId = hubEntityId;
        this.clock = clock;
    }

    private EntityDescriptor createEntityDescriptor(String entityId) {
        XMLObjectBuilderFactory openSamlBuilderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        EntityDescriptor entityDescriptor = (EntityDescriptor) openSamlBuilderFactory.getBuilder(EntityDescriptor.TYPE_NAME).buildObject(EntityDescriptor.DEFAULT_ELEMENT_NAME, EntityDescriptor.TYPE_NAME);
        entityDescriptor.setEntityID(entityId);
        return entityDescriptor;
    }

    public Document getMatchingServiceAdapterMetadata() throws ResolverException, FederationMetadataLoadingException {
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorBuilder().buildObject();
        entitiesDescriptor.setValidUntil(Instant.now(clock).plus(1, ChronoUnit.HOURS));

        final EntityDescriptor msaEntityDescriptor = createEntityDescriptor(msaConfiguration.getEntityId());
        final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        msaEntityDescriptor.getRoleDescriptors().add(getAttributeAuthorityDescriptor(openSamlXmlObjectFactory));
        msaEntityDescriptor.getRoleDescriptors().add(getIdpSsoDescriptor(openSamlXmlObjectFactory));

        entitiesDescriptor.getEntityDescriptors().add(msaEntityDescriptor);

        if (matchingServiceAdapterConfiguration.shouldRepublishHubCertificates()) {
            entitiesDescriptor.getEntityDescriptors().add(getHubEntityDescriptor());
        }

        return entitiesDescriptorElementTransformer.apply(entitiesDescriptor).getOwnerDocument();
    }

    private RoleDescriptor getIdpSsoDescriptor(OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
        IDPSSODescriptor idpssoDescriptor = openSamlXmlObjectFactory.createIDPSSODescriptor();
        idpssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        idpssoDescriptor.getSingleSignOnServices().add(getSsoService());
        idpssoDescriptor.getKeyDescriptors().addAll(getKeyDescriptors());

        return idpssoDescriptor;
    }

    private Collection<? extends KeyDescriptor> getKeyDescriptors() {
        Collection<Certificate> certificates = new ArrayList<>();
        certificates.addAll(certificateStore.getSigningCertificates());
        return keyDescriptorsUnmarshaller.fromCertificates(certificates);
    }

    private SingleSignOnService getSsoService() {
        SingleSignOnService singleSignOnService = new SingleSignOnServiceBuilder().buildObject();

        singleSignOnService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        singleSignOnService.setLocation(matchingServiceAdapterConfiguration.getHubSSOUri().toString());

        return singleSignOnService;
    }

    private RoleDescriptor getAttributeAuthorityDescriptor(OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
        final AttributeAuthorityDescriptor attributeAuthorityDescriptor = openSamlXmlObjectFactory.createAttributeAuthorityDescriptor();
        attributeAuthorityDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        final AttributeService attributeService = openSamlXmlObjectFactory.createAttributeService();
        attributeService.setLocation(msaConfiguration.getMatchingServiceAdapterExternalUrl().toASCIIString());
        attributeService.setBinding(SAMLConstants.SAML2_SOAP11_BINDING_URI);
        attributeAuthorityDescriptor.getAttributeServices().add(attributeService);

        Collection<Certificate> certificates = new ArrayList<>();
        certificates.addAll(certificateStore.getSigningCertificates());
        certificates.addAll(certificateStore.getEncryptionCertificates());
        final List<KeyDescriptor> keyDescriptors = keyDescriptorsUnmarshaller.fromCertificates(certificates);
        attributeAuthorityDescriptor.getKeyDescriptors().addAll(keyDescriptors);

        return attributeAuthorityDescriptor;
    }

    private EntityDescriptor getHubEntityDescriptor() throws ResolverException, FederationMetadataLoadingException {
        Optional<EntityDescriptor> hubDescriptorFromMetadata = Optional.ofNullable(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(hubEntityId))));
        EntityDescriptor hubEntityDescriptor = hubDescriptorFromMetadata.orElseThrow(FederationMetadataLoadingException::new);
        hubEntityDescriptor.detach();
        return hubEntityDescriptor;
    }
}
