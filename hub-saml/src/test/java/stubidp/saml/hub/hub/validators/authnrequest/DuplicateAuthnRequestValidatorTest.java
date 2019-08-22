package stubidp.saml.hub.hub.validators.authnrequest;

import io.dropwizard.util.Duration;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.hub.core.DateTimeFreezer;
import stubidp.saml.hub.hub.configuration.SamlDuplicateRequestValidationConfiguration;
import stubidp.saml.hub.test.OpenSAMLRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

public class DuplicateAuthnRequestValidatorTest extends OpenSAMLRunner {

    private DuplicateAuthnRequestValidator duplicateAuthnRequestValidator;
    private final int EXPIRATION_HOURS = 2;

    @BeforeEach
    public void initialiseTestSubject() {
        SamlDuplicateRequestValidationConfiguration samlEngineConfiguration = new SamlDuplicateRequestValidationConfiguration() {
            @Override
            public Duration getAuthnRequestIdExpirationDuration() {
                return Duration.hours(EXPIRATION_HOURS);
            }
        };
        ConcurrentMap<AuthnRequestIdKey, DateTime> duplicateIds = new ConcurrentHashMap<>();
        IdExpirationCache<AuthnRequestIdKey> idExpirationCache = new ConcurrentMapIdExpirationCache<>(duplicateIds);
        duplicateAuthnRequestValidator = new DuplicateAuthnRequestValidator(idExpirationCache, samlEngineConfiguration);
    }

    @BeforeEach
    public void freezeTime() {
        DateTimeFreezer.freezeTime();
    }

    @AfterEach
    public void unfreezeTime() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void valid_shouldThrowAnExceptionIfTheAuthnRequestIsADuplicateOfAPreviousOne() throws Exception {
        final String duplicateRequestId = "duplicate-id";
        duplicateAuthnRequestValidator.valid(duplicateRequestId);

        boolean isValid = duplicateAuthnRequestValidator.valid(duplicateRequestId);
        assertThat(isValid).isEqualTo(false);
    }

    @Test
    public void valid_shouldPassIfTheAuthnRequestIsNotADuplicateOfAPreviousOne() throws Exception {
        duplicateAuthnRequestValidator.valid("some-request-id");

        boolean isValid = duplicateAuthnRequestValidator.valid("another-request-id");
        assertThat(isValid).isEqualTo(true);
    }

    @Test
    public void valid_shouldPassIfTwoAuthnRequestsHaveTheSameIdButTheFirstAssertionHasExpired() throws Exception {
        final String duplicateRequestId = "duplicate-id";
        duplicateAuthnRequestValidator.valid(duplicateRequestId);

        DateTimeFreezer.freezeTime(DateTime.now().plusHours(EXPIRATION_HOURS).plusMinutes(1));

        boolean isValid = duplicateAuthnRequestValidator.valid(duplicateRequestId);
        assertThat(isValid).isEqualTo(true);
    }

    @Test
    public void valid_shouldFailIfAuthnRequestsReceivedWithSameIdAndFirstIdHasNotExpired() throws Exception {
        final String duplicateRequestId = "duplicate-id";
        duplicateAuthnRequestValidator.valid(duplicateRequestId);

        DateTimeFreezer.freezeTime(DateTime.now().plusHours(EXPIRATION_HOURS).minusMinutes(1));

        boolean isValid = duplicateAuthnRequestValidator.valid(duplicateRequestId);
        assertThat(isValid).isEqualTo(false);
    }
}
