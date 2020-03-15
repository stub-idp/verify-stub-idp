package stubidp.saml.hub.hub.validators.authnrequest;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.hub.hub.configuration.SamlAuthnRequestValidityDurationConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthnRequestIssueInstantValidatorTest {

    private AuthnRequestIssueInstantValidator authnRequestIssueInstantValidator = null;
    private final int AUTHN_REQUEST_VALIDITY_MINS = 5;
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));

    @BeforeEach
    public void setup() {
        SamlAuthnRequestValidityDurationConfiguration samlAuthnRequestValidityDurationConfiguration = () -> Duration.minutes(AUTHN_REQUEST_VALIDITY_MINS);
        authnRequestIssueInstantValidator = new AuthnRequestIssueInstantValidator(samlAuthnRequestValidityDurationConfiguration, clock);
    }

    @Test
    public void validate_shouldReturnFalseIfIssueInstantMoreThan5MinutesAgo() {
        Instant issueInstant = Instant.now(clock).atZone(ZoneId.of("UTC")).minusMinutes(AUTHN_REQUEST_VALIDITY_MINS).minusSeconds(1).toInstant();
        boolean validity = authnRequestIssueInstantValidator.isValid(issueInstant);
        assertThat(validity).isEqualTo(false);
    }

    @Test
    public void validate_shouldReturnTrueIfIssueInstant5MinsAgo() {
        Instant issueInstant = Instant.now(clock).atZone(ZoneId.of("UTC")).minusMinutes(AUTHN_REQUEST_VALIDITY_MINS).toInstant();
        boolean validity = authnRequestIssueInstantValidator.isValid(issueInstant);
        assertThat(validity).isEqualTo(true);
    }

    @Test
    public void validate_shouldReturnTrueIfIssueInstantLessThan5MinsAgo() {
        Instant issueInstant = Instant.now(clock).atZone(ZoneId.of("UTC")).minusMinutes(AUTHN_REQUEST_VALIDITY_MINS).plusSeconds(1).toInstant();
        boolean validity = authnRequestIssueInstantValidator.isValid(issueInstant);
        assertThat(validity).isEqualTo(true);
    }
}
