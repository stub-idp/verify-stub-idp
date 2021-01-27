package stubidp.stubidp.builders;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.OrganizationURL;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.EntitiesDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.EntityDescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.IDPSSODescriptorBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationDisplayNameBuilder;
import org.opensaml.saml.saml2.metadata.impl.OrganizationURLBuilder;
import org.opensaml.saml.saml2.metadata.impl.SingleSignOnServiceBuilder;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Document;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.domain.configuration.SamlConfiguration;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.metadata.transformers.KeyDescriptorsUnmarshaller;
import stubidp.stubidp.Urls;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.exceptions.CouldNotGenerateIdpMetadataException;
import stubidp.stubidp.repositories.Idp;
import stubidp.utils.security.security.Certificate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static stubidp.stubidp.StubIdpBinder.METADATA_VALIDITY_PERIOD;
import static stubidp.stubidp.StubIdpIdpBinder.IDP_METADATA_SIGNATURE_FACTORY;
import static stubidp.stubidp.StubIdpIdpBinder.IDP_SIGNING_CERT;

public class IdpMetadataBuilder {
    private final SignatureFactory signatureFactory;
    private final Duration validity;
    private final KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller;
    private final String idpSigningCert;
    private final SamlConfiguration samlConfiguration;
    private final XmlObjectToElementTransformer<EntitiesDescriptor> transformer = new XmlObjectToElementTransformer<>();
    private final boolean singleIdpIsEnabled;

    @Inject
    public IdpMetadataBuilder(
            @Named(IDP_METADATA_SIGNATURE_FACTORY) SignatureFactory signatureFactory,
            @Named(METADATA_VALIDITY_PERIOD) Duration validity,
            @Named(IDP_SIGNING_CERT) String idpSigningCert,
            SamlConfiguration samlConfiguration,
            StubIdpConfiguration stubIdpConfiguration) {
        this.signatureFactory = signatureFactory;
        this.validity = validity;
        this.idpSigningCert = idpSigningCert;
        this.samlConfiguration = samlConfiguration;
        this.keyDescriptorsUnmarshaller = new CoreTransformersFactory().getCertificatesToKeyDescriptorsTransformer();
        this.singleIdpIsEnabled = stubIdpConfiguration.getSingleIdpJourneyConfiguration().isEnabled();
    }

    private EntitiesDescriptor createMetadata(List<Idp> idps) throws MarshallingException, SignatureException {
        final Instant validUntil = Instant.now().plus(validity);
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorBuilder().buildObject();
        entitiesDescriptor.setValidUntil(validUntil);
        entitiesDescriptor.setID("STUB-IDP");
        for(Idp idp : idps) {
            EntityDescriptor entityDescriptor = new EntityDescriptorBuilder().buildObject();
            URI ssoEndpoint = UriBuilder.fromUri(samlConfiguration.getExpectedDestinationHost() + Urls.IDP_SAML2_SSO_RESOURCE).build(idp.getFriendlyId());
            entityDescriptor.setEntityID(idp.getIssuerId());
            entityDescriptor.setValidUntil(validUntil);
            OrganizationDisplayName organizationDisplayName = new OrganizationDisplayNameBuilder().buildObject();
            organizationDisplayName.setValue(idp.getDisplayName());
            Organization organization = new OrganizationBuilder().buildObject();
            organization.getDisplayNames().add(organizationDisplayName);
            if(singleIdpIsEnabled) {
                OrganizationURL organizationURL = new OrganizationURLBuilder().buildObject();
                organizationURL.setURI(UriBuilder.fromUri(samlConfiguration.getExpectedDestinationHost() + Urls.SINGLE_IDP_HOMEPAGE_RESOURCE).build(idp.getFriendlyId()).toASCIIString());
                organization.getURLs().add(organizationURL);
            }
            entityDescriptor.setOrganization(organization);
            entityDescriptor.getRoleDescriptors().add(getIdpSsoDescriptor(ssoEndpoint, new Certificate(idp.getIssuerId(), idpSigningCert, Certificate.KeyUse.Signing)));
            entitiesDescriptor.getEntityDescriptors().add(entityDescriptor);
        }
        return sign(entitiesDescriptor);
    }

    private RoleDescriptor getIdpSsoDescriptor(URI ssoEndpoint, Certificate signingCertificate) {
        IDPSSODescriptor idpSsoDescriptor = new IDPSSODescriptorBuilder().buildObject();
        idpSsoDescriptor.setWantAuthnRequestsSigned(true);
        idpSsoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        idpSsoDescriptor.getKeyDescriptors().addAll(keyDescriptorsUnmarshaller.fromCertificates(Collections.singletonList(signingCertificate)));
        SingleSignOnService ssoService = new SingleSignOnServiceBuilder().buildObject();
        ssoService.setLocation(ssoEndpoint.toString());
        ssoService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        idpSsoDescriptor.getSingleSignOnServices().add(ssoService);
        return idpSsoDescriptor;
    }

    private <T extends SignableSAMLObject> T sign(T signableSAMLObject) throws MarshallingException, SignatureException {
        signableSAMLObject.setSignature(signatureFactory.createSignature());
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signableSAMLObject).marshall(signableSAMLObject);
        Signer.signObject(signableSAMLObject.getSignature());
        return signableSAMLObject;
    }

    public Document getSignedMetadata(Idp idp) {
        try {
            return transformer.apply(createMetadata(List.of(idp))).getOwnerDocument();
        } catch (MarshallingException | SignatureException e) {
            throw new CouldNotGenerateIdpMetadataException(e);
        }
    }
}
