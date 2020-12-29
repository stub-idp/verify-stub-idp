package stubidp.saml.hub.domain;

import org.opensaml.xmlsec.signature.Signature;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class AuthnRequestFromRelyingParty extends VerifySamlMessage {

    private Optional<Boolean> forceAuthentication;
    private Optional<URI> assertionConsumerServiceUrl;
    private Optional<Integer> assertionConsumerServiceIndex;
    private Optional<Signature> signature;
    private Optional<String> verifyServiceProviderVersion;

    protected AuthnRequestFromRelyingParty() {
    }

    public AuthnRequestFromRelyingParty(
        String id,
        String issuer,
        Instant issueInstant,
        URI destination,
        Optional<Boolean> forceAuthentication,
        Optional<URI> assertionConsumerServiceUrl,
        Optional<Integer> assertionConsumerServiceIndex,
        Optional<Signature> signature,
        Optional<String> verifyServiceProviderVersion
    ) {
        super(id, issuer, issueInstant, destination);
        this.forceAuthentication = forceAuthentication;
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        this.assertionConsumerServiceIndex = assertionConsumerServiceIndex;
        this.signature = signature;
        this.verifyServiceProviderVersion = verifyServiceProviderVersion;
    }

    public Optional<Boolean> getForceAuthentication() {
        return forceAuthentication;
    }

    public Optional<Integer> getAssertionConsumerServiceIndex() {
        return assertionConsumerServiceIndex;
    }

    public Optional<Signature> getSignature() {
        return signature;
    }

    public Optional<URI> getAssertionConsumerServiceUrl() {
        return assertionConsumerServiceUrl;
    }

    public Optional<String> getVerifyServiceProviderVersion() {
        return verifyServiceProviderVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthnRequestFromRelyingParty that = (AuthnRequestFromRelyingParty) o;
        return Objects.equals(forceAuthentication, that.forceAuthentication) && Objects.equals(assertionConsumerServiceUrl, that.assertionConsumerServiceUrl) && Objects.equals(assertionConsumerServiceIndex, that.assertionConsumerServiceIndex) && Objects.equals(signature, that.signature) && Objects.equals(verifyServiceProviderVersion, that.verifyServiceProviderVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(forceAuthentication, assertionConsumerServiceUrl, assertionConsumerServiceIndex, signature, verifyServiceProviderVersion);
    }

    @Override
    public String toString() {
        return "AuthnRequestFromRelyingParty{" +
            "forceAuthentication=" + forceAuthentication +
            ", assertionConsumerServiceUrl=" + assertionConsumerServiceUrl +
            ", assertionConsumerServiceIndex=" + assertionConsumerServiceIndex +
            ", signature=" + signature +
            ", verifyServiceProviderVersion=" + verifyServiceProviderVersion +
            '}';
    }
}
