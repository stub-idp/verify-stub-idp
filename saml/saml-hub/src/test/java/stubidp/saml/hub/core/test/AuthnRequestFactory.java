package stubidp.saml.hub.core.test;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.AuthnRequestBuilder;
import stubidp.saml.test.builders.IssuerBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

class AuthnRequestFactory {
    private final Function<AuthnRequest, String> authnRequestToStringTransformer;

    public AuthnRequestFactory(Function<AuthnRequest, String> authnRequestToStringTransformer) {
        this.authnRequestToStringTransformer = authnRequestToStringTransformer;
    }

    public String anAuthnRequest(
            String id,
            String issuer,
            Optional<Boolean> forceAuthentication,
            Optional<URI> assertionConsumerServiceUrl,
            Optional<Integer> assertionConsumerServiceIndex,
            String publicCert,
            String privateKey,
            String ssoRequestEndpoint,
            Optional<Instant> issueInstant) {
        AuthnRequest authnRequest = getAuthnRequest(
                id,
                issuer,
                forceAuthentication,
                assertionConsumerServiceUrl,
                assertionConsumerServiceIndex,
                publicCert,
                privateKey,
                ssoRequestEndpoint,
                issueInstant);
        return authnRequestToStringTransformer.apply(authnRequest);
    }

    private AuthnRequest getAuthnRequest(
            String id,
            String issuer,
            Optional<Boolean> forceAuthentication,
            Optional<URI> assertionConsumerServiceUrl,
            Optional<Integer> assertionConsumerServiceIndex,
            String publicCert,
            String privateKey,
            String ssoRequestEndpoint,
            Optional<Instant> issueInstant) {
        AuthnRequestBuilder authnRequestBuilder = AuthnRequestBuilder.anAuthnRequest()
                .withId(id)
                .withIssuer(IssuerBuilder.anIssuer().withIssuerId(issuer).build())
                .withDestination("http://localhost" + ssoRequestEndpoint)
                .withSigningCredential(new TestCredentialFactory(publicCert, privateKey).getSigningCredential());

        forceAuthentication.ifPresent(authnRequestBuilder::withForceAuthn);
        assertionConsumerServiceIndex.ifPresent(authnRequestBuilder::withAssertionConsumerServiceIndex);
        assertionConsumerServiceUrl.ifPresent(uri -> authnRequestBuilder.withAssertionConsumerServiceUrl(uri.toString()));
        issueInstant.ifPresent(authnRequestBuilder::withIssueInstant);

        return authnRequestBuilder.build();
    }

    public String anInvalidAuthnRequest(
            String id,
            String issuer,
            Optional<Boolean> forceAuthentication,
            Optional<URI> assertionConsumerServiceUrl,
            Optional<Integer> assertionConsumerServiceIndex,
            String publicCert,
            String privateKey,
            String ssoRequestEndpoint,
            Optional<Instant> issueInstant) {
        // Pad ID to ensure request is long enough
        AuthnRequest authnRequest = getAuthnRequest(
                id + "x".repeat(1200),
                issuer,
                forceAuthentication,
                assertionConsumerServiceUrl,
                assertionConsumerServiceIndex,
                publicCert,
                privateKey,
                ssoRequestEndpoint,
                issueInstant);
        authnRequest.setSignature(null);
        // Use a different transformer to ensure that no Signature elements are added
        XmlObjectToBase64EncodedStringTransformer<XMLObject> transformer = new XmlObjectToBase64EncodedStringTransformer<>();
        return transformer.apply(authnRequest);
    }
}
