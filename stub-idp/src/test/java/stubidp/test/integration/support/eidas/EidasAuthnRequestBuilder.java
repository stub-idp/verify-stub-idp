package stubidp.test.integration.support.eidas;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.core.impl.ExtensionsBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.RequestedAttribute;
import stubidp.saml.extensions.extensions.SPType;
import stubidp.saml.extensions.extensions.impl.RequestedAttributeBuilder;
import stubidp.saml.extensions.extensions.impl.RequestedAttributesBuilder;
import stubidp.saml.extensions.extensions.impl.RequestedAttributesImpl;
import stubidp.saml.extensions.extensions.impl.SPTypeBuilder;
import stubidp.saml.hub.hub.domain.LevelOfAssurance;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.utils.core.test.builders.AuthnRequestBuilder;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class EidasAuthnRequestBuilder {
    private String issuerEntityId = "issuerEntityId";
    private String destination = "destination";
    private DateTime issueInstant = DateTime.now();
    private List<RequestedAttribute> requestedAttributeList = new ArrayList<>();
    private boolean withKeyInfo = true;
    private boolean withInvalidKey = false;
    private boolean removeAllX509Datas = false;
    private boolean removeAllCertificates = false;

    public static EidasAuthnRequestBuilder anAuthnRequest() {
        return new EidasAuthnRequestBuilder();
    }

    public EidasAuthnRequestBuilder withIssuerEntityId(String issuerEntityId) {
        this.issuerEntityId = issuerEntityId;
        return this;
    }

    public EidasAuthnRequestBuilder withDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public EidasAuthnRequestBuilder withIssueInstant(DateTime issueInstant) {
        this.issueInstant = issueInstant;
        return this;
    }

    public EidasAuthnRequestBuilder withRequestedAttribute(String requestedAttributeName) {
        requestedAttributeList.add(createRequestedAttribute(requestedAttributeName));
        return this;
    }

    public EidasAuthnRequestBuilder withKeyInfo(boolean withKeyInfo) {
        this.withKeyInfo = withKeyInfo;
        return this;
    }

    public EidasAuthnRequestBuilder withInvalidKey(boolean withInvalidKey) {
        this.withInvalidKey = withInvalidKey;
        return this;
    }

    public EidasAuthnRequestBuilder removeAllX509Datas(boolean removeAllX509Datas) {
        this.removeAllX509Datas = removeAllX509Datas;
        return this;
    }

    public EidasAuthnRequestBuilder removeAllCertificates(boolean removeAllCertificates) {
        this.removeAllCertificates = removeAllCertificates;
        return this;
    }

    public AuthnRequest buildAuthnRequest() {
        AuthnRequest authnRequest = AuthnRequestBuilder.anAuthnRequest()
                .withId(new SecureRandomIdentifierGenerationStrategy().generateIdentifier())
                .withDestination(destination)
                .withIssueInstant(issueInstant)
                .withIssuer(createIssuer(issuerEntityId))
                .withNameIdPolicy(createNameIDPolicy())
                .build();

        authnRequest.setExtensions(createEidasExtensions());
        authnRequest.setRequestedAuthnContext(createRequestedAuthnContext(
                AuthnContextComparisonTypeEnumeration.MINIMUM,
                LevelOfAssurance.SUBSTANTIAL.toString()));

        authnRequest.setSignature(createSignature());

        try {
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnRequest).marshall(authnRequest);
            Signer.signObject(authnRequest.getSignature());
        } catch (SignatureException | MarshallingException e) {
            throw new RuntimeException(e);
        }

        return authnRequest;
    }

    public String build() {
        AuthnRequest authnRequest = buildAuthnRequest();
        if(removeAllCertificates) {
            authnRequest.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().clear();
        }
        if(removeAllX509Datas) {
            authnRequest.getSignature().getKeyInfo().getX509Datas().clear();
        }
        return new XmlObjectToBase64EncodedStringTransformer<>().apply(authnRequest);
    }

    private Signature createSignature() {
        IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(createIdaKeyStore());

        SignatureFactory signatureFactory = new SignatureFactory(withKeyInfo, keyStoreCredentialRetriever, new SignatureRSASHA256(), new DigestSHA256());

        return signatureFactory.createSignature();
    }

    private IdaKeyStore createIdaKeyStore() {
        return withInvalidKey?createInvalidIdaKeyStore():createValidIdaKeyStore();
    }

    private IdaKeyStore createValidIdaKeyStore() {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());

        PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_SIGNING_KEY));
        PublicKey publicSigningKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT);

        PrivateKey privateEncKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
        KeyPair encryptionKeyPair = new KeyPair(publicEncKey, privateEncKey);

        X509Certificate certificate = new X509CertificateFactory().createCertificate(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT);
        return new IdaKeyStore(certificate, signingKeyPair, Arrays.asList(encryptionKeyPair));
    }

    private IdaKeyStore createInvalidIdaKeyStore() {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());

        PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY));
        PublicKey publicSigningKey = publicKeyFactory.createPublicKey(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);

        PrivateKey privateEncKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncKey = publicKeyFactory.createPublicKey(TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT);

        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
        KeyPair encryptionKeyPair = new KeyPair(publicEncKey, privateEncKey);

        X509Certificate certificate = new X509CertificateFactory().createCertificate(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);
        return new IdaKeyStore(certificate, signingKeyPair, Arrays.asList(encryptionKeyPair));
    }

    private RequestedAuthnContext createRequestedAuthnContext(AuthnContextComparisonTypeEnumeration comparisonType, String loa) {
        RequestedAuthnContext requestedAuthnContext = new RequestedAuthnContextBuilder().buildObject();
        requestedAuthnContext.setComparison(comparisonType);

        AuthnContextClassRef authnContextClassRef = new AuthnContextClassRefBuilder().buildObject();
        authnContextClassRef.setAuthnContextClassRef(loa);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        return requestedAuthnContext;
    }

    private NameIDPolicy createNameIDPolicy() {
        NameIDPolicy nameIDPolicy = new NameIDPolicyBuilder().buildObject();
        nameIDPolicy.setAllowCreate(true);
        nameIDPolicy.setFormat(NameIDType.PERSISTENT);
        return nameIDPolicy;
    }

    private Extensions createEidasExtensions() {
        SPType spType = new SPTypeBuilder().buildObject();
        spType.setValue("public");

        RequestedAttributesImpl requestedAttributes = (RequestedAttributesImpl)new RequestedAttributesBuilder().buildObject();

        // required attributes
        requestedAttributeList.add(createRequestedAttribute(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME));
        requestedAttributeList.add(createRequestedAttribute(IdaConstants.Eidas_Attributes.FamilyName.NAME));
        requestedAttributeList.add(createRequestedAttribute(IdaConstants.Eidas_Attributes.FirstName.NAME));
        requestedAttributeList.add(createRequestedAttribute(IdaConstants.Eidas_Attributes.DateOfBirth.NAME));
        requestedAttributes.setRequestedAttributes(requestedAttributeList.toArray(new RequestedAttribute[] {}));

        Extensions extensions = new ExtensionsBuilder().buildObject();
        extensions.getUnknownXMLObjects().add(spType);
        extensions.getUnknownXMLObjects().add(requestedAttributes);
        return extensions;
    }

    private Issuer createIssuer(String issuerEntityId) {
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue(issuerEntityId);
        return issuer;
    }

    private RequestedAttribute createRequestedAttribute(String requestedAttributeName) {
        RequestedAttribute attr = new RequestedAttributeBuilder().buildObject();
        attr.setName(requestedAttributeName);
        attr.setNameFormat(Attribute.URI_REFERENCE);
        attr.setIsRequired(true);
        return attr;
    }
}
