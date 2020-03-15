package stubidp.stubidp.builders;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.SingleSignOnServiceBuilder;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.metadata.transformers.KeyDescriptorsUnmarshaller;
import stubidp.utils.security.security.Certificate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.namespace.QName;
import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static stubidp.stubidp.StubIdpBinder.METADATA_VALIDITY_PERIOD;

public class CountryMetadataBuilder {
    private final Duration validity;
    private final KeyDescriptorsUnmarshaller keyDescriptorsUnmarshaller;
    private final XMLObjectBuilderFactory xmlFactory;
    private final CountryMetadataSigningHelper metadataSigner;

    @Inject
    public CountryMetadataBuilder(
            @Named(METADATA_VALIDITY_PERIOD) Duration validity,
            CountryMetadataSigningHelper metadataSigner) {
        this.validity = validity;
        this.metadataSigner = metadataSigner;
        this.keyDescriptorsUnmarshaller = new CoreTransformersFactory().getCertificatesToKeyDescriptorsTransformer();
        this.xmlFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
    }

    public EntityDescriptor createEntityDescriptorForProxyNodeService(
        URI entityId,
        URI ssoEndpoint,
        java.security.cert.Certificate signingCertificate,
        java.security.cert.Certificate encryptingCertificate
    ) throws MarshallingException, SecurityException, SignatureException, CertificateEncodingException {
        EntityDescriptor entityDescriptor = createElement(EntityDescriptor.DEFAULT_ELEMENT_NAME, EntityDescriptor.TYPE_NAME);
        entityDescriptor.setEntityID(entityId.toString());
        entityDescriptor.setValidUntil(Instant.now().plus(validity));
        entityDescriptor.getRoleDescriptors().add(getIdpSsoDescriptor(entityId, ssoEndpoint, signingCertificate, encryptingCertificate));

        metadataSigner.sign(entityDescriptor);
        return entityDescriptor;
    }

    private RoleDescriptor getIdpSsoDescriptor(
        URI entityId,
        URI ssoEndpoint,
        java.security.cert.Certificate signingCertificate,
        java.security.cert.Certificate encryptingCertificate
    ) throws CertificateEncodingException {
        IDPSSODescriptor idpSsoDescriptor = createElement(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, IDPSSODescriptor.TYPE_NAME);
        idpSsoDescriptor.setWantAuthnRequestsSigned(true);
        idpSsoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        List<Certificate> certificates = Arrays.asList(
          getSigningCertificate(entityId, signingCertificate),
          getEncryptingCertificate(entityId, encryptingCertificate));
        idpSsoDescriptor.getKeyDescriptors().addAll(keyDescriptorsUnmarshaller.fromCertificates(certificates));

        SingleSignOnService ssoService = new SingleSignOnServiceBuilder().buildObject();
        ssoService.setLocation(ssoEndpoint.toString());
        ssoService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        idpSsoDescriptor.getSingleSignOnServices().add(ssoService);

        return idpSsoDescriptor;
    }

    @SuppressWarnings("unchecked")
    private <T extends XMLObject> T createElement(QName elementName, QName typeName) {
        return (T) xmlFactory.getBuilder(elementName).buildObject(elementName, typeName);
    }

    private Certificate getSigningCertificate(URI entityId, java.security.cert.Certificate signingCertificate) throws CertificateEncodingException {
        return new Certificate(entityId.toString(), encodeBase64String(signingCertificate.getEncoded()), Certificate.KeyUse.Signing);
    }

    private Certificate getEncryptingCertificate(URI entityId, java.security.cert.Certificate encryptingCertificate) throws CertificateEncodingException {
        return new Certificate(entityId.toString(), encodeBase64String(encryptingCertificate.getEncoded()), Certificate.KeyUse.Encryption);
    }
}
