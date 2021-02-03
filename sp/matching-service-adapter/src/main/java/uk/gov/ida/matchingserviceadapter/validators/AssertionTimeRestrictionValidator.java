package uk.gov.ida.matchingserviceadapter.validators;

import stubidp.saml.utils.core.validation.SamlResponseValidationException;

import javax.inject.Inject;
import java.time.Instant;

public class AssertionTimeRestrictionValidator {

    private final DateTimeComparator dateTimeComparator;

    @Inject
    public AssertionTimeRestrictionValidator(DateTimeComparator dateTimeComparator) {
        this.dateTimeComparator = dateTimeComparator;
    }

    public void validateNotOnOrAfter(Instant notOnOrAfter) {
        if (dateTimeComparator.isBeforeNow(notOnOrAfter)) {
            throw new SamlResponseValidationException(String.format(
                "Assertion is not valid on or after %s",
                notOnOrAfter.toString()
            ));
        }
    }

    public void validateNotBefore(Instant notBefore) {
        if (notBefore != null && dateTimeComparator.isAfterSkewedNow(notBefore)) {
            throw new SamlResponseValidationException(String.format(
                "Assertion is not valid before %s",
                notBefore.toString()
            ));
        }
    }
}