package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.Conditions;
import stubidp.saml.utils.core.validation.conditions.AudienceRestrictionValidator;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsElementMustNotBeNull;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsShouldNotContainOneTimeUseElement;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsShouldNotContainProxyRestrictionElement;

import javax.inject.Inject;
import java.time.Instant;

public class IdpConditionsValidator implements ConditionsValidator {

    private final AssertionTimeRestrictionValidator timeRestrictionValidator;
    private final AudienceRestrictionValidator audienceRestrictionValidator;

    @Inject
    public IdpConditionsValidator(
            AssertionTimeRestrictionValidator timeRestrictionValidator,
            AudienceRestrictionValidator audienceRestrictionValidator
    ) {
        this.timeRestrictionValidator = timeRestrictionValidator;
        this.audienceRestrictionValidator = audienceRestrictionValidator;
    }

    public void validate(Conditions conditionsElement, String... acceptableEntityIds) {

        ConditionsElementMustNotBeNull.validate(conditionsElement);

        ConditionsShouldNotContainProxyRestrictionElement.validate(conditionsElement);

        ConditionsShouldNotContainOneTimeUseElement.validate(conditionsElement);

        Instant notOnOrAfter = conditionsElement.getNotOnOrAfter();
        if (notOnOrAfter != null) {
            timeRestrictionValidator.validateNotOnOrAfter(notOnOrAfter);
        }

        timeRestrictionValidator.validateNotBefore(conditionsElement.getNotBefore());
        audienceRestrictionValidator.validate(conditionsElement.getAudienceRestrictions(), acceptableEntityIds);
    }
}
