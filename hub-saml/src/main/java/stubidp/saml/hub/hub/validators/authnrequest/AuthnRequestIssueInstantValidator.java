package stubidp.saml.hub.hub.validators.authnrequest;

import stubidp.saml.hub.hub.configuration.SamlAuthnRequestValidityDurationConfiguration;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class AuthnRequestIssueInstantValidator {
    private final SamlAuthnRequestValidityDurationConfiguration samlAuthnRequestValidityDurationConfiguration;
    private final Clock clock;

    @Inject
    public AuthnRequestIssueInstantValidator(SamlAuthnRequestValidityDurationConfiguration samlAuthnRequestValidityDurationConfiguration) {
        this(samlAuthnRequestValidityDurationConfiguration, Clock.systemUTC());
    }

    AuthnRequestIssueInstantValidator(SamlAuthnRequestValidityDurationConfiguration samlAuthnRequestValidityDurationConfiguration,
                                             Clock clock) {
        this.samlAuthnRequestValidityDurationConfiguration = samlAuthnRequestValidityDurationConfiguration;
        this.clock = clock;
    }

    public boolean isValid(Instant issueInstant) {
        final Duration authnRequestValidityDuration = samlAuthnRequestValidityDurationConfiguration.getAuthnRequestValidityDuration();
        return !issueInstant.isBefore(Instant.now(clock).minus(authnRequestValidityDuration));
    }
}
