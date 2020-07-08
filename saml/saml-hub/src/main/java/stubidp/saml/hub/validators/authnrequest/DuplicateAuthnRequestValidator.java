package stubidp.saml.hub.validators.authnrequest;

import stubidp.saml.hub.configuration.SamlDuplicateRequestValidationConfiguration;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class DuplicateAuthnRequestValidator {
    private final IdExpirationCache<AuthnRequestIdKey> previousRequests;
    private final Duration expirationDuration;
    private final Clock clock;

    @Inject
    public DuplicateAuthnRequestValidator(IdExpirationCache<AuthnRequestIdKey> previousRequests,
                                          SamlDuplicateRequestValidationConfiguration samlDuplicateRequestValidationConfiguration) {
        this(previousRequests, samlDuplicateRequestValidationConfiguration, Clock.systemUTC());
    }

    DuplicateAuthnRequestValidator(IdExpirationCache<AuthnRequestIdKey> previousRequests,
                                          SamlDuplicateRequestValidationConfiguration samlDuplicateRequestValidationConfiguration,
                                          Clock clock) {
        this.previousRequests = previousRequests;
        this.expirationDuration = samlDuplicateRequestValidationConfiguration.getAuthnRequestIdExpirationDuration();
        this.clock = clock;
    }

    public boolean valid(String requestId) {
        AuthnRequestIdKey key = new AuthnRequestIdKey(requestId);
        if (previousRequests.contains(key) && previousRequests.getExpiration(key).isAfter(Instant.now(clock))) {
            return false;
        }
        Instant expire = Instant.now(clock).plus(expirationDuration);
        previousRequests.setExpiration(key, expire);
        return true;
    }
}
