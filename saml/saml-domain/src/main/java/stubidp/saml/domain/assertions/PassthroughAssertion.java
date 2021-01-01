package stubidp.saml.domain.assertions;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.FraudDetectedDetails;
import stubidp.saml.domain.assertions.PersistentId;

import java.io.Serializable;
import java.util.Optional;

public class PassthroughAssertion implements Serializable {
    private final PersistentId persistentId;
    // this is optional because this is used for both AuthnStatement and MDS Assertions
    private final Optional<AuthnContext> authnContext;
    private final String underlyingAssertionBlob;
    private final Optional<FraudDetectedDetails> fraudDetectedDetails;
    private final Optional<String> principalIpAddressAsSeenByIdp;

    public PassthroughAssertion(
            PersistentId persistentId,
            Optional<AuthnContext> levelOfAssurance,
            String underlyingAssertionBlob,
            Optional<FraudDetectedDetails> fraudDetectedDetails,
            Optional<String> principalIpAddressAsSeenByIdp) {

        this.persistentId = persistentId;
        this.authnContext = levelOfAssurance;
        this.underlyingAssertionBlob = underlyingAssertionBlob;
        this.fraudDetectedDetails = fraudDetectedDetails;
        this.principalIpAddressAsSeenByIdp = principalIpAddressAsSeenByIdp;
    }

    public String getUnderlyingAssertionBlob() {
        return underlyingAssertionBlob;
    }

    public Optional<AuthnContext> getAuthnContext() {
        return authnContext;
    }

    public PersistentId getPersistentId() {
        return persistentId;
    }

    public boolean isFraudulent() {
        return authnContext.isPresent() && authnContext.get() == AuthnContext.LEVEL_X;
    }

    public Optional<FraudDetectedDetails> getFraudDetectedDetails() {
        return fraudDetectedDetails;
    }

    public Optional<String> getPrincipalIpAddressAsSeenByIdp() {
        return principalIpAddressAsSeenByIdp;
    }
}
