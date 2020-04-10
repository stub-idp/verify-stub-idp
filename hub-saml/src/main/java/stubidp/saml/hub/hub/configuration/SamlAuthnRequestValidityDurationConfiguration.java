package stubidp.saml.hub.hub.configuration;

import java.time.Duration;

public interface SamlAuthnRequestValidityDurationConfiguration {
    Duration getAuthnRequestValidityDuration();
}
