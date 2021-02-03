package uk.gov.ida.matchingserviceadapter.validator.validationrules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.OneTimeUse;
import org.opensaml.saml.saml2.core.ProxyRestriction;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsElementMustNotBeNull;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsShouldNotContainOneTimeUseElement;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsShouldNotContainProxyRestrictionElement;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidationRulesTests {
    private Conditions conditions;

    @BeforeEach
    public void setUp() {
        conditions = mock(Conditions.class);
    }

    @Test
    public void shouldThrowExceptionWhenConditionsElementIsNull() {
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> ConditionsElementMustNotBeNull.validate(null))
                .withMessage("Conditions is missing from the assertion.");
    }

    @Test
    public void shouldNotThrowExceptionWhenConditionsElementIsNotNull() {
        ConditionsElementMustNotBeNull.validate(conditions);
    }

    @Test
    public void shouldThrowExceptionWhenConditionsContainOneTimeUseElement() {
        when(conditions.getOneTimeUse()).thenReturn(mock(OneTimeUse.class));

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> ConditionsShouldNotContainOneTimeUseElement.validate(conditions))
                .withMessage("Conditions should not contain one time use element.");
    }

    @Test
    public void shouldNotThrowExceptionWhenConditionsDoNotContainOneTimeUseElement() {
        ConditionsShouldNotContainOneTimeUseElement.validate(conditions);
    }

    @Test
    public void shouldThrowExceptionWhenConditionsContainProxyRestrictionElement() {
        when(conditions.getProxyRestriction()).thenReturn(mock(ProxyRestriction.class));

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> ConditionsShouldNotContainProxyRestrictionElement.validate(conditions))
                .withMessage("Conditions should not contain proxy restriction element.");
    }

    @Test
    public void shouldNotThrowExceptionWhenConditionsDoNotContainProxyRestrictionElement() {
        ConditionsShouldNotContainProxyRestrictionElement.validate(conditions);
    }
}
