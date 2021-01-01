package stubsp.stubsp.saml.request;

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
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.UsageType;
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
import stubidp.saml.hub.domain.LevelOfAssurance;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EidasAuthnRequestBuilder {
    private static final RequestedAttribute[] REQUESTED_ATTRIBUTES = {};

    private String issuerEntityId = "issuerEntityId";
    private String destination = "destination";
    private Instant issueInstant = Instant.now();
    private final List<RequestedAttribute> requestedAttributeList = new ArrayList<>();
    private boolean withKeyInfo = true;
    private boolean removeAllX509Datas = false;
    private boolean removeAllCertificates = false;
    private IdaKeyStore idaKeyStore;

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

    public EidasAuthnRequestBuilder withIssueInstant(Instant issueInstant) {
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

    public EidasAuthnRequestBuilder removeAllX509Datas(boolean removeAllX509Datas) {
        this.removeAllX509Datas = removeAllX509Datas;
        return this;
    }

    public EidasAuthnRequestBuilder removeAllCertificates(boolean removeAllCertificates) {
        this.removeAllCertificates = removeAllCertificates;
        return this;
    }

    public AuthnRequest buildAuthnRequest() {
        BasicCredential credential = new BasicCredential(idaKeyStore.getSigningKeyPair().getPublic(), idaKeyStore.getSigningKeyPair().getPrivate());
        credential.setUsageType(UsageType.SIGNING);

        AuthnRequest authnRequest = AuthnRequestBuilder.anAuthnRequest()
                .withId(AuthnRequestIdGenerator.generateRequestId())
                .withDestination(destination)
                .withIssueInstant(issueInstant)
                .withIssuer(createIssuer(issuerEntityId))
                .withNameIdPolicy(createNameIDPolicy())
                .withSigningCredential(credential)
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
        IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(idaKeyStore);

        SignatureFactory signatureFactory = new SignatureFactory(withKeyInfo, keyStoreCredentialRetriever, new SignatureRSASHA256(), new DigestSHA256());

        return signatureFactory.createSignature();
    }

    public EidasAuthnRequestBuilder withKeyStore(IdaKeyStore idaKeyStore) {
        this.idaKeyStore = idaKeyStore;
        return this;
    }

    private RequestedAuthnContext createRequestedAuthnContext(AuthnContextComparisonTypeEnumeration comparisonType, String loa) {
        RequestedAuthnContext requestedAuthnContext = new RequestedAuthnContextBuilder().buildObject();
        requestedAuthnContext.setComparison(comparisonType);

        AuthnContextClassRef authnContextClassRef = new AuthnContextClassRefBuilder().buildObject();
        authnContextClassRef.setURI(loa);
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
        requestedAttributes.setRequestedAttributes(requestedAttributeList.toArray(REQUESTED_ATTRIBUTES));

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
