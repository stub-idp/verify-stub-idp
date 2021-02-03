package uk.gov.ida.matchingserviceadapter.validators.validationrules;

import org.opensaml.saml.saml2.core.Conditions;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;

import java.util.Objects;
import java.util.function.Predicate;

public class ConditionsElementMustNotBeNull extends ValidationRule<Conditions> {

    private ConditionsElementMustNotBeNull() {}

    @Override
    protected Predicate<Conditions> getPredicate() {
        return Objects::nonNull;
    }

    @Override
    protected void throwException() {
        throw new SamlResponseValidationException("Conditions is missing from the assertion.");
    }

    public static void validate(Conditions conditions) {
        new ConditionsElementMustNotBeNull().apply(conditions);
    }
}
