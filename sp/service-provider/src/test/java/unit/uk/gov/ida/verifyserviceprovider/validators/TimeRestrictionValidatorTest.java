package unit.uk.gov.ida.verifyserviceprovider.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;
import uk.gov.ida.verifyserviceprovider.validators.TimeRestrictionValidator;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class TimeRestrictionValidatorTest {

    @Mock
    private DateTimeComparator dateTimeComparator;

    private TimeRestrictionValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new TimeRestrictionValidator(dateTimeComparator);
    }

    @Test
    public void validateNotOnOrAfterShouldThrowExceptionWhenNotOnOrAfterIsBeforeNow() {
        Instant notOnOrAfter = Instant.now();
        when(dateTimeComparator.isBeforeNow(notOnOrAfter)).thenReturn(true);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validateNotOnOrAfter(notOnOrAfter))
                .withMessage(String.format(
                        "Assertion is not valid on or after %s",
                        notOnOrAfter
                ));
    }

    @Test
    public void validateNotBeforeShouldThrowExceptionWhenNotBeforeIsAfterNow() {
        Instant notBefore = Instant.now();
        when(dateTimeComparator.isAfterNow(notBefore)).thenReturn(true);

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validateNotBefore(notBefore))
                .withMessage(String.format(
                        "Assertion is not valid before %s",
                        notBefore
                ));
    }
}