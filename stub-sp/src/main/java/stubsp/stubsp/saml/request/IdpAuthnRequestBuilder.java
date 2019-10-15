package stubsp.stubsp.saml.request;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;

import java.util.Optional;
import java.util.UUID;

public class IdpAuthnRequestBuilder {

    private static XmlObjectToBase64EncodedStringTransformer<AuthnRequest> toBase64EncodedStringTransformer = new XmlObjectToBase64EncodedStringTransformer<>();

    private String destination;
    private String entityId;
    private DateTime issueInstant = DateTime.now();
    private boolean withKeyInfo = false;
    private Credential signingCredential;
    private String signingCertificate;

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

    public IdpAuthnRequestBuilder withSigningCredential(Credential signingCredential) {
        this.signingCredential = signingCredential;
        return this;
    }

    public IdpAuthnRequestBuilder withSigningCertificate(String signingCertificate) {
        this.signingCertificate = signingCertificate;
        return this;
    }

    public String build() {
        return anAuthnRequest("_"+UUID.randomUUID().toString(),
                entityId,
                Optional.of(false),
                Optional.of(0),
                signingCredential,
                destination,
                Optional.ofNullable(issueInstant),
                withKeyInfo?Optional.ofNullable(signingCertificate):Optional.empty());
    }

    private static String anAuthnRequest(
            String id,
            String issuer,
            Optional<Boolean> forceAuthentication,
            Optional<Integer> assertionConsumerServiceIndex,
            Credential signingCredential,
            String ssoRequestEndpoint,
            Optional<DateTime> issueInstant,
            Optional<String> x509Certificate) {
        AuthnRequestBuilder authnRequestBuilder = AuthnRequestBuilder.anAuthnRequest()
                .withId(id)
                .withIssuer(IssuerBuilder.anIssuer().withIssuerId(issuer).build())
                .withDestination(ssoRequestEndpoint)
                .withSigningCredential(signingCredential);

        x509Certificate.ifPresent(authnRequestBuilder::withX509Certificate);
        forceAuthentication.ifPresent(authnRequestBuilder::withForceAuthn);
        assertionConsumerServiceIndex.ifPresent(authnRequestBuilder::withAssertionConsumerServiceIndex);
        issueInstant.ifPresent(authnRequestBuilder::withIssueInstant);

        return toBase64EncodedStringTransformer.apply(authnRequestBuilder.build());
    }
}
