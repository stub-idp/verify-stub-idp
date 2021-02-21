package unit.uk.gov.ida.verifyserviceprovider.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class InstantValidatorTest {

    @Mock
    private DateTimeComparator dateTimeComparator;

    private InstantValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new InstantValidator(dateTimeComparator);
    }

    @Test
    public void shouldValidateInstantIsInExpectedRange() {
        Instant instant = Instant.now().minus(1, ChronoUnit.MINUTES);

        validator.validate(instant, "any-instant-name");
    }

    @Test
    public void shouldThrowExceptionIfInstantOldenThanFiveMinutes() {
        Instant instant = Instant.now().minus(6, ChronoUnit.MINUTES);
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validate(instant, "some-instant-name"))
                .withMessageContaining("some-instant-name is too far in the past ");
    }

    @Test
    public void shouldThrowExceptionWhenInstantIsInTheFuture() {
        Instant instant = Instant.now().plus(1, ChronoUnit.MINUTES);
        String errorMessage = String.format("%s is in the future %s",
                "some-instant-name",
                instant);

        when(dateTimeComparator.isAfterNow(instant)).thenReturn(true);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validate(instant, "some-instant-name"))
                .withMessage(errorMessage);
    }
}