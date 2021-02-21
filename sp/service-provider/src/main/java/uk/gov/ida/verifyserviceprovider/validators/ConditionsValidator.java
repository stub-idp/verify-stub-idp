package uk.gov.ida.verifyserviceprovider.validators;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Conditions;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import stubidp.saml.utils.core.validation.conditions.AudienceRestrictionValidator;

import java.time.Instant;

public class ConditionsValidator {

    private final TimeRestrictionValidator timeRestrictionValidator;
    private final AudienceRestrictionValidator audienceRestrictionValidator;

    public ConditionsValidator(
        TimeRestrictionValidator timeRestrictionValidator,
        AudienceRestrictionValidator audienceRestrictionValidator
    ) {
        this.timeRestrictionValidator = timeRestrictionValidator;
        this.audienceRestrictionValidator = audienceRestrictionValidator;
    }

    public void validate(Conditions conditionsElement, String... acceptableEntityIds) {
        if (conditionsElement == null) {
            throw new SamlResponseValidationException("Conditions is missing from the assertion.");
        }

        if (conditionsElement.getProxyRestriction() != null) {
            throw new SamlResponseValidationException("Conditions should not contain proxy restriction element.");
        }

        if (conditionsElement.getOneTimeUse() != null) {
            throw new SamlResponseValidationException("Conditions should not contain one time use element.");
        }

        Instant notOnOrAfter = conditionsElement.getNotOnOrAfter();
        if (notOnOrAfter != null) {
            timeRestrictionValidator.validateNotOnOrAfter(notOnOrAfter);
        }

        timeRestrictionValidator.validateNotBefore(conditionsElement.getNotBefore());
        audienceRestrictionValidator.validate(conditionsElement.getAudienceRestrictions(), acceptableEntityIds);
    }
}
