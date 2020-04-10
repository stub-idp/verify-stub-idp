package stubidp.saml.hub.hub.configuration;

import java.time.Duration;

public interface SamlDuplicateRequestValidationConfiguration {
    Duration getAuthnRequestIdExpirationDuration();
}
