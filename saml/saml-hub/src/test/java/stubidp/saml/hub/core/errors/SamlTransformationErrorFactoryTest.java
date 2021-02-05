package stubidp.saml.hub.core.errors;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SamlTransformationErrorFactoryTest {

    @Test
    void shouldHaveLevelWarnForDuplicateMatchingDataset() {
        SamlValidationSpecificationFailure failure =
                SamlTransformationErrorFactory.duplicateMatchingDataset("id", "responseIssuerId");
        assertThat(failure.getLogLevel()).isEqualTo(Level.WARN);
    }

    @Test
    void shouldHaveLevelWarnForExceededNotOnOrAfter() {
        SamlValidationSpecificationFailure failure =
                SamlTransformationErrorFactory.exceededNotOnOrAfter(Instant.now());
        assertThat(failure.getLogLevel()).isEqualTo(Level.WARN);
    }

    @Test // arbitrary choice of error
    void shouldHaveLevelErrorForMissingIssueInstant() {
        SamlValidationSpecificationFailure failure =
                SamlTransformationErrorFactory.missingIssueInstant("id");
        assertThat(failure.getLogLevel()).isEqualTo(Level.ERROR);
    }
}