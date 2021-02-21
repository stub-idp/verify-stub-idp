package uk.gov.ida.verifyserviceprovider.validators;

import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import java.time.Duration;
import java.time.Instant;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;

public class InstantValidator {

    private static final Duration MAXIMUM_INSTANT_AGE = Duration.ofMinutes(5);
    private final DateTimeComparator dateTimeComparator;

    public InstantValidator(DateTimeComparator dateTimeComparator) {
        this.dateTimeComparator = dateTimeComparator;
    }

    public void validate(Instant instant, String instantName) {
        if (Instant.now().minus(MAXIMUM_INSTANT_AGE).isAfter(instant)) {
            throw new SamlResponseValidationException(String.format("%s is too far in the past %s",
                instantName,
                instant
            ));
        }

        if (dateTimeComparator.isAfterNow(instant)) {
            throw new SamlResponseValidationException(String.format("%s is in the future %s",
                instantName,
                instant
            ));
        }
    }
}
