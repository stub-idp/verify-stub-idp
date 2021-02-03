package uk.gov.ida.matchingserviceadapter.validators;

import stubidp.saml.utils.core.validation.SamlResponseValidationException;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class InstantValidator {

    private static final Duration MAXIMUM_INSTANT_AGE = Duration.ofMinutes(5);
    private final DateTimeComparator dateTimeComparator;

    @Inject
    public InstantValidator(DateTimeComparator dateTimeComparator) {
        this.dateTimeComparator = dateTimeComparator;
    }

    public void validate(Instant instant, String instantName) {
        if (dateTimeComparator.isAfterSkewedNow(instant)) {
            throw new SamlResponseValidationException(String.format("%s is in the future %s",
                    instantName,
                    instant.toString()));
        }

        if (!dateTimeComparator.isBeforeFuzzy(Instant.now(), instant.plus(MAXIMUM_INSTANT_AGE))) {
            throw new SamlResponseValidationException(String.format("%s is too far in the past %s",
                    instantName,
                    instant.toString()));
        }
    }
}
