package stubidp.saml.hub.hub.validators.authnrequest;

import io.dropwizard.util.Duration;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.hub.hub.configuration.SamlAuthnRequestValidityDurationConfiguration;
import stubidp.utils.common.datetime.DateTimeFreezer;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthnRequestIssueInstantValidatorTest {

    AuthnRequestIssueInstantValidator authnRequestIssueInstantValidator = null;
    private final int AUTHN_REQUEST_VALIDITY_MINS = 5;

    @BeforeEach
    public void setup() {
        SamlAuthnRequestValidityDurationConfiguration samlAuthnRequestValidityDurationConfiguration = new SamlAuthnRequestValidityDurationConfiguration() {
            @Override
            public Duration getAuthnRequestValidityDuration() {
                return Duration.minutes(AUTHN_REQUEST_VALIDITY_MINS);
            }
        };

        authnRequestIssueInstantValidator = new AuthnRequestIssueInstantValidator(samlAuthnRequestValidityDurationConfiguration);
    }

    @AfterEach
    public void unfreezeTime() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void validate_shouldReturnFalseIfIssueInstantMoreThan5MinutesAgo() {
        DateTimeFreezer.freezeTime();

        DateTime issueInstant = DateTime.now().minusMinutes(AUTHN_REQUEST_VALIDITY_MINS).minusSeconds(1);

        boolean validity = authnRequestIssueInstantValidator.isValid(issueInstant);

        assertThat(validity).isEqualTo(false);
    }

    @Test
    public void validate_shouldReturnTrueIfIssueInstant5MinsAgo() {
        DateTimeFreezer.freezeTime();

        DateTime issueInstant = DateTime.now().minusMinutes(AUTHN_REQUEST_VALIDITY_MINS);

        boolean validity = authnRequestIssueInstantValidator.isValid(issueInstant);

        assertThat(validity).isEqualTo(true);
    }

    @Test
    public void validate_shouldReturnTrueIfIssueInstantLessThan5MinsAgo() {
        DateTimeFreezer.freezeTime();

        DateTime issueInstant = DateTime.now().minusMinutes(AUTHN_REQUEST_VALIDITY_MINS).plusSeconds(1);

        boolean validity = authnRequestIssueInstantValidator.isValid(issueInstant);

        assertThat(validity).isEqualTo(true);
    }
}
