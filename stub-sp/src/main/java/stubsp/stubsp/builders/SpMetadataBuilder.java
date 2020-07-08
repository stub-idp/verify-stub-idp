package stubsp.stubsp.builders;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.OrganizationURL;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.opensaml.saml.saml2.metadata.impl.EntitiesDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.EntityDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationDisplayNameBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationURLBuilder;
import org.opensaml.saml.saml2.metadata.impl.SPSSODescriptorBuilder;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Document;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.domain.configuration.SamlConfiguration;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.metadata.transformers.KeyDescriptorsUnmarshaller;
import stubidp.utils.security.security.Certificate;
import stubsp.stubsp.Urls;
import stubsp.stubsp.exceptions.CouldNotGenerateSpMetadataException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static stubsp.stubsp.StubSpBinder.METADATA_VALIDITY_PERIOD;
import static stubsp.stubsp.StubSpBinder.SERVICE_NAME;
import static stubsp.stubsp.StubSpBinder.SP_ENCRYPTION_CERT;
import static stubsp.stubsp.StubSpBinder.SP_METADATA_SIGNATURE_FACTORY;
import static stubsp.stubsp.StubSpBinder.SP_SIGNING_CERT;

public class SpMetadataBuilder {
    private final SignatureFactory signatureFactory;
    private final Duration validity;
    private final KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller;
    private final String spSigningCert;
    private final String spEncryptionCert;
    private final SamlConfiguration samlConfiguration;
    private final XmlObjectToElementTransformer<EntitiesDescriptor> transformer = new XmlObjectToElementTransformer<>();
    private final String serviceName;

    @Inject
    public SpMetadataBuilder(
            @Named(SP_METADATA_SIGNATURE_FACTORY) SignatureFactory signatureFactory,
            @Named(METADATA_VALIDITY_PERIOD) Duration validity,
            @Named(SP_SIGNING_CERT) String spSigningCert,
            @Named(SP_ENCRYPTION_CERT) String spEncryptionCert,
            SamlConfiguration samlConfiguration,
            @Named(SERVICE_NAME) String serviceName) {
        this.signatureFactory = signatureFactory;
        this.validity = validity;
        this.spSigningCert = spSigningCert;
        this.spEncryptionCert = spEncryptionCert;
        this.samlConfiguration = samlConfiguration;
        this.keyDescriptorsUnmarshaller = new CoreTransformersFactory().getCertificatesToKeyDescriptorsTransformer();
        this.serviceName = serviceName;
    }

    private EntitiesDescriptor createMetadata() throws MarshallingException, SignatureException {
        final Instant validUntil = Instant.now().plus(validity);
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorBuilder().buildObject();
        entitiesDescriptor.setValidUntil(validUntil);
        entitiesDescriptor.setID("STUB-SP");
        EntityDescriptor entityDescriptor = new EntityDescriptorBuilder().buildObject();
        URI ssoEndpoint = UriBuilder.fromUri(samlConfiguration.getExpectedDestinationHost() + Urls.SAML_SSO_RESPONSE_RESOURCE).build();
        entityDescriptor.setEntityID(samlConfiguration.getEntityId());
        entityDescriptor.setValidUntil(validUntil);
        OrganizationDisplayName organizationDisplayName = new OrganizationDisplayNameBuilder().buildObject();
        organizationDisplayName.setValue(serviceName);
        Organization organization = new OrganizationBuilder().buildObject();
        organization.getDisplayNames().add(organizationDisplayName);
        OrganizationURL organizationURL = new OrganizationURLBuilder().buildObject();
        organizationURL.setURI(UriBuilder.fromUri(samlConfiguration.getExpectedDestinationHost() + Urls.ROOT_RESOURCE).build().toASCIIString());
        organization.getURLs().add(organizationURL);
        entityDescriptor.setOrganization(organization);
        entityDescriptor.getRoleDescriptors().add(getSpSsoDescriptor(ssoEndpoint,
                List.of(new Certificate(samlConfiguration.getEntityId(), spSigningCert, Certificate.KeyUse.Signing),
                        new Certificate(samlConfiguration.getEntityId(), spEncryptionCert, Certificate.KeyUse.Encryption))));
        entitiesDescriptor.getEntityDescriptors().add(entityDescriptor);
        return sign(entitiesDescriptor);
    }

    private SPSSODescriptor getSpSsoDescriptor(URI ssoEndpoint, List<Certificate> certificates) {
        SPSSODescriptor spssoDescriptor = new SPSSODescriptorBuilder().buildObject();
        spssoDescriptor.setAuthnRequestsSigned(true);
        spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        spssoDescriptor.getKeyDescriptors().addAll(keyDescriptorsUnmarshaller.fromCertificates(certificates));
        AssertionConsumerService assertionConsumerService = new AssertionConsumerServiceBuilder().buildObject();
        assertionConsumerService.setLocation(ssoEndpoint.toString());
        assertionConsumerService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        spssoDescriptor.getAssertionConsumerServices().add(assertionConsumerService);
        return spssoDescriptor;
    }

    private <T extends SignableSAMLObject> T sign(T signableSAMLObject) throws MarshallingException, SignatureException {
        signableSAMLObject.setSignature(signatureFactory.createSignature());
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signableSAMLObject).marshall(signableSAMLObject);
        Signer.signObject(signableSAMLObject.getSignature());
        return signableSAMLObject;
    }

    public Document getSignedMetadata() {
        try {
            return transformer.apply(createMetadata()).getOwnerDocument();
        } catch (MarshallingException | SignatureException e) {
            throw new CouldNotGenerateSpMetadataException(e);
        }
    }
}
