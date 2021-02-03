package uk.gov.ida.matchingserviceadapter.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.AssertionTimeRestrictionValidator;
import uk.gov.ida.matchingserviceadapter.validators.DateTimeComparator;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class TimeRestrictionValidatorTest {

    private DateTimeComparator dateTimeComparator;

    private AssertionTimeRestrictionValidator validator;

    @BeforeEach
    public void setUp() {
        dateTimeComparator = new DateTimeComparator(Duration.ofMillis(5000));

        validator = new AssertionTimeRestrictionValidator(dateTimeComparator);
    }

    @Test
    public void validateNotOnOrAfterShouldThrowExceptionWhenNotOnOrAfterIsBeforeNow() {
        Instant notOnOrAfter = Instant.now();
        String errorMessage = String.format(
                "Assertion is not valid on or after %s",
                notOnOrAfter.toString()
        );

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validateNotOnOrAfter(notOnOrAfter))
                .withMessage(errorMessage);
    }

    @Test
    public void validateNotBeforeShouldThrowExceptionWhenNotBeforeIsAfterNow() {
        Instant notBefore = Instant.now().plusMillis(6000);
        String errorMessage = String.format(
                "Assertion is not valid before %s",
                notBefore.toString()
        );

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validateNotBefore(notBefore))
                .withMessage(errorMessage);
    }
}