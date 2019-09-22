package stubidp.test.integration.support;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.AuthnRequest;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.utils.core.test.TestCredentialFactory;
import stubidp.saml.utils.core.test.builders.IssuerBuilder;

import java.util.Optional;
import java.util.UUID;

import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;

public class IdpAuthnRequestBuilder {

    private static XmlObjectToBase64EncodedStringTransformer<AuthnRequest> toBase64EncodedStringTransformer = new XmlObjectToBase64EncodedStringTransformer<>();

    private String destination = "destination";
    private String entityId = HUB_ENTITY_ID;
    private DateTime issueInstant = DateTime.now();
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

    public IdpAuthnRequestBuilder withIssueInstant(DateTime issueInstant) {
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
                Optional.ofNullable(false),
                Optional.ofNullable(0),
                withInvalidKey?TEST_RP_MS_PUBLIC_SIGNING_CERT:HUB_TEST_PUBLIC_SIGNING_CERT,
                withInvalidKey?TEST_RP_MS_PRIVATE_SIGNING_KEY:HUB_TEST_PRIVATE_SIGNING_KEY,
                destination,
                Optional.ofNullable(issueInstant),
                withKeyInfo?Optional.ofNullable(withInvalidKey?TEST_RP_MS_PUBLIC_SIGNING_CERT:HUB_TEST_PUBLIC_SIGNING_CERT):Optional.empty());
    }

    private static String anAuthnRequestX(
            String id,
            String issuer,
            Optional<Boolean> forceAuthentication,
            Optional<Integer> assertionConsumerServiceIndex,
            String publicCert,
            String privateKey,
            String ssoRequestEndpoint,
            Optional<DateTime> issueInstant,
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
            Optional<DateTime> issueInstant,
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
