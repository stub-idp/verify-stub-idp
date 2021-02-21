package uk.gov.ida.verifyserviceprovider.validators;

import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import java.time.Instant;

public class TimeRestrictionValidator {

    private final DateTimeComparator dateTimeComparator;

    public TimeRestrictionValidator(DateTimeComparator dateTimeComparator) {
        this.dateTimeComparator = dateTimeComparator;
    }

    public void validateNotOnOrAfter(Instant notOnOrAfter) {
        if (dateTimeComparator.isBeforeNow(notOnOrAfter)) {
            throw new SamlResponseValidationException(String.format(
                "Assertion is not valid on or after %s",
                notOnOrAfter
            ));
        }
    }

    public void validateNotBefore(Instant notBefore) {
        if (notBefore != null && dateTimeComparator.isAfterNow(notBefore)) {
            throw new SamlResponseValidationException(String.format(
                "Assertion is not valid before %s",
                notBefore
            ));
        }
    }
}
