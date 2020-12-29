package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.request.EidasAuthnRequestFromHub;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EidasAuthnRequestBuilder {
    private String id = UUID.randomUUID().toString();
    private String issuer = "issuer_id";
    private final Instant issueInstant = Instant.now();
    private URI destination = URI.create("http://eidas/ssoLocation");
    private String providerName;
    private List<AuthnContext> authnContextList;

    private EidasAuthnRequestBuilder() {}

    public static EidasAuthnRequestBuilder anEidasAuthnRequest() {
        return new EidasAuthnRequestBuilder();
    }

    public EidasAuthnRequestFromHub buildFromHub() {
        return new EidasAuthnRequestFromHub(
            id,
            issuer,
            issueInstant,
            authnContextList,
            destination,
            providerName);
    }

    public EidasAuthnRequestBuilder withLevelsOfAssurance(List<AuthnContext> authnContextList) {
        this.authnContextList = authnContextList;
        return this;
    }

    public EidasAuthnRequestBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public EidasAuthnRequestBuilder withDestination(String destination) {
        this.destination = URI.create(destination);
        return this;
    }

    public EidasAuthnRequestBuilder withProviderName(String providerName) {
        this.providerName = providerName;
        return this;
    }

    public EidasAuthnRequestBuilder withIssuer(String issuerEntityId) {
        this.issuer = issuerEntityId;
        return this;
    }
}
