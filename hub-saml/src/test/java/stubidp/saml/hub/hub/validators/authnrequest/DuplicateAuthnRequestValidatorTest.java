package stubidp.saml.hub.hub.validators.authnrequest;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.hub.core.OpenSAMLRunner;
import stubidp.saml.hub.hub.configuration.SamlDuplicateRequestValidationConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

public class DuplicateAuthnRequestValidatorTest extends OpenSAMLRunner {
    private final int EXPIRATION_HOURS = 2;
    private final SamlDuplicateRequestValidationConfiguration samlEngineConfiguration = () -> Duration.hours(EXPIRATION_HOURS);
    private Clock clock = Clock.systemUTC();
    private IdExpirationCache<AuthnRequestIdKey> idExpirationCache;
    private ConcurrentMap<AuthnRequestIdKey, Instant> duplicateIds;
    private DuplicateAuthnRequestValidator duplicateAuthnRequestValidator;

    @BeforeEach
    public void initialiseTestSubject() {
        duplicateIds = new ConcurrentHashMap<>();
        idExpirationCache = new ConcurrentMapIdExpirationCache<>(duplicateIds);
    }

    @AfterEach
    public void unfreezeTime() {
        clock = Clock.systemUTC();
    }

    @Test
    public void valid_shouldThrowAnExceptionIfTheAuthnRequestIsADuplicateOfAPreviousOne() {
        final String duplicateRequestId = "duplicate-id";
        duplicateAuthnRequestValidator = new DuplicateAuthnRequestValidator(idExpirationCache, samlEngineConfiguration, clock);
        duplicateAuthnRequestValidator.valid(duplicateRequestId);
        boolean isValid = duplicateAuthnRequestValidator.valid(duplicateRequestId);
        assertThat(isValid).isEqualTo(false);
    }

    @Test
    public void valid_shouldPassIfTheAuthnRequestIsNotADuplicateOfAPreviousOne() {
        duplicateAuthnRequestValidator = new DuplicateAuthnRequestValidator(idExpirationCache, samlEngineConfiguration, clock);
        duplicateAuthnRequestValidator.valid("some-request-id");
        boolean isValid = duplicateAuthnRequestValidator.valid("another-request-id");
        assertThat(isValid).isEqualTo(true);
    }

    @Test
    public void valid_shouldPassIfTwoAuthnRequestsHaveTheSameIdButTheFirstAssertionHasExpired() {
        final String duplicateRequestId = "duplicate-id";
        duplicateAuthnRequestValidator = new DuplicateAuthnRequestValidator(idExpirationCache, samlEngineConfiguration, clock);
        duplicateAuthnRequestValidator.valid(duplicateRequestId);
        clock = Clock.fixed(Instant.now().atZone(ZoneId.of("UTC")).plusHours(EXPIRATION_HOURS).plusMinutes(1).toInstant(), ZoneId.of("UTC"));
        // reinitialise the validator but ~~ in the future ~~ using the existing idExpirationCache
        duplicateAuthnRequestValidator = new DuplicateAuthnRequestValidator(idExpirationCache, samlEngineConfiguration, clock);
        boolean isValid = duplicateAuthnRequestValidator.valid(duplicateRequestId);
        assertThat(isValid).isEqualTo(true);
    }

    @Test
    public void valid_shouldFailIfAuthnRequestsReceivedWithSameIdAndFirstIdHasNotExpired() {
        final String duplicateRequestId = "duplicate-id";
        clock = Clock.fixed(Instant.now().atZone(ZoneId.of("UTC")).plusHours(EXPIRATION_HOURS).minusMinutes(1).toInstant(), ZoneId.of("UTC"));
        duplicateAuthnRequestValidator = new DuplicateAuthnRequestValidator(idExpirationCache, samlEngineConfiguration, clock);
        duplicateAuthnRequestValidator.valid(duplicateRequestId);
        boolean isValid = duplicateAuthnRequestValidator.valid(duplicateRequestId);
        assertThat(isValid).isEqualTo(false);
    }
}
