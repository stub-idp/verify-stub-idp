package stubsp.stubsp.saml.request;

import com.google.common.base.Strings;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Scoping;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import java.time.Instant;
import java.util.Optional;

public class AuthnRequestBuilder {

    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private Optional<NameIDPolicy> nameIdPolicy = Optional.empty();
    private Optional<Scoping> scoping = Optional.empty();
    private Optional<String> assertionConsumerServiceUrl = Optional.empty();
    private Optional<String> protocolBinding = Optional.of(SAMLConstants.SAML2_POST_BINDING_URI);
    private Optional<Boolean> isPassive = Optional.empty();

    private Optional<Issuer> issuer = Optional.ofNullable(IssuerBuilder.anIssuer().build());
    private Optional<String> id = Optional.of(AuthnRequestIdGenerator.generateRequestId());
    private Optional<String> minimumLevelOfAssurance = Optional.of(IdaAuthnContext.LEVEL_1_AUTHN_CTX);
    private boolean shouldSign = true;
    private boolean shouldAddSignature = true;
    private Optional<Instant> issueInstant = Optional.of(Instant.now());

    private Optional<String> versionNumber = Optional.of(IdaConstants.SAML_VERSION_NUMBER);
    private Optional<String> destination = Optional.empty();
    private Optional<String> requiredLevelOfAssurance = Optional.of(IdaAuthnContext.LEVEL_2_AUTHN_CTX);
    private Optional<Credential> signingCredential = Optional.empty();
    private Optional<Boolean> forceAuthn = Optional.empty();
    private Optional<Integer> assertionConsumerServiceIndex = Optional.empty();
    private Optional<String> x509Certificate = Optional.empty();

    public static AuthnRequestBuilder anAuthnRequest() {
        return new AuthnRequestBuilder();
    }

    public AuthnRequest build() {

        AuthnRequest authnRequest = openSamlXmlObjectFactory.createAuthnRequest();

        issuer.ifPresent(authnRequest::setIssuer);
        id.ifPresent(authnRequest::setID);

        versionNumber.ifPresentOrElse(v -> authnRequest.setVersion(openSamlXmlObjectFactory.createSamlVersion(v)),
                () -> authnRequest.setVersion(null));

        if (minimumLevelOfAssurance.isPresent() || requiredLevelOfAssurance.isPresent()) {
            RequestedAuthnContext requestedAuthnContext = openSamlXmlObjectFactory.createRequestedAuthnContext(AuthnContextComparisonTypeEnumeration.MINIMUM);
            authnRequest.setRequestedAuthnContext(requestedAuthnContext);
            minimumLevelOfAssurance.ifPresent(loa -> requestedAuthnContext.getAuthnContextClassRefs().add(openSamlXmlObjectFactory.createAuthnContextClassReference(loa)));
            requiredLevelOfAssurance.ifPresent(loa -> requestedAuthnContext.getAuthnContextClassRefs().add(openSamlXmlObjectFactory.createAuthnContextClassReference(loa)));
        }

        nameIdPolicy.ifPresent(authnRequest::setNameIDPolicy);
        scoping.ifPresent(authnRequest::setScoping);
        assertionConsumerServiceUrl.ifPresent(authnRequest::setAssertionConsumerServiceURL);
        protocolBinding.ifPresent(authnRequest::setProtocolBinding);
        isPassive.ifPresent(authnRequest::setIsPassive);
        issueInstant.ifPresent(authnRequest::setIssueInstant);
        destination.ifPresent(authnRequest::setDestination);
        forceAuthn.ifPresent(authnRequest::setForceAuthn);
        assertionConsumerServiceIndex.ifPresent(authnRequest::setAssertionConsumerServiceIndex);

        authnRequest.setConditions(ConditionsBuilder.aConditions().build());

        //This must be the last thing done before returning; otherwise, the signature will be invalidated
        if (issuer.isPresent() && !Strings.isNullOrEmpty(issuer.get().getValue()) && shouldAddSignature) {
            final SignatureBuilder signatureBuilder = SignatureBuilder.aSignature();
            id.ifPresent(signatureBuilder::withId);
            x509Certificate.ifPresent(signatureBuilder::withX509Data);
            signingCredential.ifPresent(signatureBuilder::withSigningCredential);
            signatureBuilder.withSignatureAlgorithm(new SignatureRSASHA256());
            signatureBuilder.withDigestAlgorithm(new DigestSHA256());
            authnRequest.setSignature(signatureBuilder.build());
            try {
                XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnRequest).marshall(authnRequest);
                if (shouldSign) {
                    Signer.signObject(authnRequest.getSignature());
                }
            } catch (SignatureException | MarshallingException e) {
                throw new RuntimeException(e);
            }
        }

        return authnRequest;
    }

    public AuthnRequestBuilder withoutSignatureElement() {
        shouldAddSignature = false;
        return this;
    }

    public AuthnRequestBuilder withoutSigning() {
        shouldSign = false;
        return this;
    }

    public AuthnRequestBuilder withId(String id) {
        this.id = Optional.ofNullable(id);
        return this;
    }

    public AuthnRequestBuilder withIssuer(Issuer issuer) {
        this.issuer = Optional.ofNullable(issuer);
        return this;
    }

    public AuthnRequestBuilder withNameIdPolicy(NameIDPolicy policy) {
        this.nameIdPolicy = Optional.of(policy);
        return this;
    }

    public AuthnRequestBuilder withScoping(Scoping scoping) {
        this.scoping = Optional.ofNullable(scoping);
        return this;
    }

    public AuthnRequestBuilder withAssertionConsumerServiceUrl(String url) {
        this.assertionConsumerServiceUrl = Optional.ofNullable(url);
        return this;
    }

    public AuthnRequestBuilder withProtocolBinding(String protocolBinding) {
        this.protocolBinding = Optional.ofNullable(protocolBinding);
        return this;
    }

    public AuthnRequestBuilder withIsPassive(boolean isPassive) {
        this.isPassive = Optional.of(isPassive);
        return this;
    }

    public AuthnRequestBuilder withIssueInstant(Instant instant) {
        this.issueInstant = Optional.ofNullable(instant);
        return this;
    }

    public AuthnRequestBuilder withVersionNumber(String versionNumber) {
        this.versionNumber = Optional.ofNullable(versionNumber);
        return this;
    }

    public AuthnRequestBuilder withDestination(String destination) {
        this.destination = Optional.ofNullable(destination);
        return this;
    }

    public AuthnRequestBuilder withSigningCredential(Credential credential) {
        this.signingCredential = Optional.ofNullable(credential);
        this.shouldAddSignature = true;
        return this;
    }

    public AuthnRequestBuilder withX509Certificate(String x509Certificate) {
        this.x509Certificate = Optional.ofNullable(x509Certificate);
        return this;
    }

    public AuthnRequestBuilder withForceAuthn(Boolean forceAuthn) {
        this.forceAuthn = Optional.of(forceAuthn);
        return this;
    }

    public void withAssertionConsumerServiceIndex(Integer index) {
        this.assertionConsumerServiceIndex = Optional.of(index);
    }
}
