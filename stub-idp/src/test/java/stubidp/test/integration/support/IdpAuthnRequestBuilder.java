package stubidp.test.integration.support;

import org.opensaml.saml.saml2.core.AuthnRequest;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.IssuerBuilder;
import stubsp.stubsp.saml.request.AuthnRequestBuilder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;

public class IdpAuthnRequestBuilder {
    private static final XmlObjectToBase64EncodedStringTransformer<AuthnRequest> toBase64EncodedStringTransformer = new XmlObjectToBase64EncodedStringTransformer<>();

    private String destination = "destination";
    private String entityId = HUB_ENTITY_ID;
    private Instant issueInstant = Instant.now();
    private boolean withInvalidKey = false;
    private boolean withKeyInfo = false;

    private IdpAuthnRequestBuilder() {}

    public static IdpAuthnRequestBuilder anAuthnRequest() {
        return new IdpAuthnRequestBuilder();
    }

    public IdpAuthnRequestBuilder withDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public IdpAuthnRequestBuilder withEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public IdpAuthnRequestBuilder withKeyInfo(boolean withKeyInfo) {
        this.withKeyInfo = withKeyInfo;
        return this;
    }

    public IdpAuthnRequestBuilder withIssueInstant(Instant issueInstant) {
        this.issueInstant = issueInstant;
        return this;
    }

    public IdpAuthnRequestBuilder withInvalidKey(boolean withInvalidKey) {
        this.withInvalidKey = withInvalidKey;
        return this;
    }

    public String build() {
        return anAuthnRequestX("_"+UUID.randomUUID().toString(),
                entityId,
                Optional.of(false),
                Optional.of(0),
                withInvalidKey?TEST_RP_MS_PUBLIC_SIGNING_CERT:HUB_TEST_PUBLIC_SIGNING_CERT,
                withInvalidKey?TEST_RP_MS_PRIVATE_SIGNING_KEY:HUB_TEST_PRIVATE_SIGNING_KEY,
                destination,
                Optional.ofNullable(issueInstant),
                withKeyInfo?Optional.of(withInvalidKey?TEST_RP_MS_PUBLIC_SIGNING_CERT:HUB_TEST_PUBLIC_SIGNING_CERT):Optional.empty());
    }

    private static String anAuthnRequestX(
            String id,
            String issuer,
            Optional<Boolean> forceAuthentication,
            Optional<Integer> assertionConsumerServiceIndex,
            String publicCert,
            String privateKey,
            String ssoRequestEndpoint,
            Optional<Instant> issueInstant,
            Optional<String> x509Certificate) {
        AuthnRequest authnRequest = getAuthnRequest(id, issuer, forceAuthentication, assertionConsumerServiceIndex, publicCert, privateKey, ssoRequestEndpoint, issueInstant, x509Certificate);
        return toBase64EncodedStringTransformer.apply(authnRequest);
    }

    private static AuthnRequest getAuthnRequest(
            String id,
            String issuer,
            Optional<Boolean> forceAuthentication,
            Optional<Integer> assertionConsumerServiceIndex,
            String publicCert,
            String privateKey,
            String ssoRequestEndpoint,
            Optional<Instant> issueInstant,
            Optional<String> x509Certificate) {
        AuthnRequestBuilder authnRequestBuilder = AuthnRequestBuilder.anAuthnRequest()
                .withId(id)
                .withIssuer(IssuerBuilder.anIssuer().withIssuerId(issuer).build())
                .withDestination(ssoRequestEndpoint)
                .withSigningCredential(new TestCredentialFactory(publicCert, privateKey).getSigningCredential());

        x509Certificate.ifPresent(authnRequestBuilder::withX509Certificate);
        forceAuthentication.ifPresent(authnRequestBuilder::withForceAuthn);
        assertionConsumerServiceIndex.ifPresent(authnRequestBuilder::withAssertionConsumerServiceIndex);
        issueInstant.ifPresent(authnRequestBuilder::withIssueInstant);

        return authnRequestBuilder.build();
    }
}
