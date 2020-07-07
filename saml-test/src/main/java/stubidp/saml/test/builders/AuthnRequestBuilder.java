package stubidp.saml.test.builders;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Scoping;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.test.OpenSamlXmlObjectFactory;
import stubidp.saml.test.support.AuthnRequestIdGenerator;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class AuthnRequestBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private boolean shouldSign = true;
    private boolean shouldAddSignature = true;
    private SignatureAlgorithm signatureAlgorithm = new SignatureRSASHA256();
    private DigestAlgorithm digestAlgorithm = new DigestSHA256();

    private Optional<NameIDPolicy> nameIdPolicy = empty();
    private Optional<Scoping> scoping = empty();
    private Optional<String> assertionConsumerServiceUrl = empty();
    private Optional<String> protocolBinding = ofNullable(SAMLConstants.SAML2_POST_BINDING_URI);
    private Optional<Boolean> isPassive = empty();

    private Optional<Issuer> issuer = ofNullable(IssuerBuilder.anIssuer().build());
    private Optional<String> id = ofNullable(AuthnRequestIdGenerator.generateRequestId());
    private Optional<String> minimumLevelOfAssurance = ofNullable(IdaAuthnContext.LEVEL_1_AUTHN_CTX);
    private Optional<Instant> issueInstant = ofNullable(Instant.now());

    private Optional<String> versionNumber = ofNullable(IdaConstants.SAML_VERSION_NUMBER);
    private Optional<String> destination = empty();
    private Optional<String> requiredLevelOfAssurance = ofNullable(IdaAuthnContext.LEVEL_2_AUTHN_CTX);
    private Optional<Credential> signingCredential = empty();
    private Optional<Boolean> forceAuthn = empty();
    private Optional<Integer> assertionConsumerServiceIndex = empty();

    private AuthnRequestBuilder() {}

    public static AuthnRequestBuilder anAuthnRequest() {
        return new AuthnRequestBuilder();
    }

    public AuthnRequest build() {
        AuthnRequest authnRequest = openSamlXmlObjectFactory.createAuthnRequest();

        issuer.ifPresent(authnRequest::setIssuer);
        id.ifPresent(authnRequest::setID);
        if (versionNumber.isPresent()) {
            authnRequest.setVersion(openSamlXmlObjectFactory.createSamlVersion(versionNumber.get()));
        } else {
            authnRequest.setVersion(null);
        }

        if (minimumLevelOfAssurance.isPresent() || requiredLevelOfAssurance.isPresent()) {
            RequestedAuthnContext requestedAuthnContext = openSamlXmlObjectFactory.createRequestedAuthnContext(AuthnContextComparisonTypeEnumeration.MINIMUM);
            authnRequest.setRequestedAuthnContext(requestedAuthnContext);
            if (minimumLevelOfAssurance.isPresent()) {
                AuthnContextClassRef authnContextClassReference = openSamlXmlObjectFactory.createAuthnContextClassReference(minimumLevelOfAssurance.get());
                requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassReference);
            }
            if (requiredLevelOfAssurance.isPresent()) {
                AuthnContextClassRef authnContextClassReference = openSamlXmlObjectFactory.createAuthnContextClassReference(requiredLevelOfAssurance.get());
                requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassReference);
            }
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

        //This must be the last thing done before returning; otherwise, the signature will be invalidated
        if (issuer.isPresent() && Objects.nonNull(issuer.get().getValue()) && !issuer.get().getValue().isBlank() && shouldAddSignature) {
            final SignatureBuilder signatureBuilder = SignatureBuilder.aSignature().withSignatureAlgorithm(signatureAlgorithm);
            id.ifPresent(s -> signatureBuilder.withDigestAlgorithm(s, digestAlgorithm));
            signingCredential.ifPresent(signatureBuilder::withSigningCredential);
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
        this.id = ofNullable(id);
        return this;
    }

    public AuthnRequestBuilder withIssuer(Issuer issuer) {
        this.issuer = ofNullable(issuer);
        return this;
    }

    public AuthnRequestBuilder withNameIdPolicy(NameIDPolicy policy) {
        this.nameIdPolicy = ofNullable(policy);
        return this;
    }

    public AuthnRequestBuilder withScoping(Scoping scoping) {
        this.scoping = ofNullable(scoping);
        return this;
    }

    public AuthnRequestBuilder withAssertionConsumerServiceUrl(String url) {
        this.assertionConsumerServiceUrl = ofNullable(url);
        return this;
    }

    public AuthnRequestBuilder withProtocolBinding(String protocolBinding) {
        this.protocolBinding = ofNullable(protocolBinding);
        return this;
    }

    public AuthnRequestBuilder withIsPassive(boolean isPassive) {
        this.isPassive = Optional.of(isPassive);
        return this;
    }

    public AuthnRequestBuilder withIssueInstant(Instant instant) {
        this.issueInstant = ofNullable(instant);
        return this;
    }

    public AuthnRequestBuilder withVersionNumber(String versionNumber) {
        this.versionNumber = ofNullable(versionNumber);
        return this;
    }

    public AuthnRequestBuilder withDestination(String destination) {
        this.destination = ofNullable(destination);
        return this;
    }

    public AuthnRequestBuilder withSigningCredential(Credential credential) {
        this.signingCredential = ofNullable(credential);
        this.shouldAddSignature = true;
        return this;
    }

    public AuthnRequestBuilder withForceAuthn(Boolean forceAuthn) {
        this.forceAuthn = Optional.of(forceAuthn);
        return this;
    }

    public AuthnRequestBuilder withSignatureAlgorithm(@NotNull SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        return this;
    }

    public AuthnRequestBuilder withDigestAlgorithm(@NotNull DigestAlgorithm digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
        return this;
    }

    public void withAssertionConsumerServiceIndex(Integer index) {
        this.assertionConsumerServiceIndex = Optional.of(index);
    }

}
