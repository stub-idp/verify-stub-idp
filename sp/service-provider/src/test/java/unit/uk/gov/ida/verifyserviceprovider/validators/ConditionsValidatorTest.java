package unit.uk.gov.ida.verifyserviceprovider.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.OneTimeUse;
import org.opensaml.saml.saml2.core.ProxyRestriction;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import stubidp.saml.utils.core.validation.conditions.AudienceRestrictionValidator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.TimeRestrictionValidator;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.test.builders.AudienceRestrictionBuilder.anAudienceRestriction;

@ExtendWith({MockitoExtension.class})
public class ConditionsValidatorTest extends OpenSAMLRunner {

    @Mock
    private TimeRestrictionValidator timeRestrictionValidator;
    @Mock
    private AudienceRestrictionValidator audienceRestrictionValidator;
    @Mock
    private Conditions conditions;

    private ConditionsValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new ConditionsValidator(timeRestrictionValidator, audienceRestrictionValidator);
    }

    @Test
    public void shouldThrowExceptionWhenConditionsIsNull() {
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validate(null, "any-entity-id"))
                .withMessage("Conditions is missing from the assertion.");
    }

    @Test
    public void shouldThrowExceptionWhenProxyRestrictionElementExists() {
        when(conditions.getProxyRestriction()).thenReturn(mock(ProxyRestriction.class));

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validate(conditions, "any-entity-id"))
                .withMessage("Conditions should not contain proxy restriction element.");
    }

    @Test
    public void shouldThrowExceptionWhenOneTimeUseElementExists() {
        when(conditions.getOneTimeUse()).thenReturn(mock(OneTimeUse.class));

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validate(conditions, "any-entity-id"))
                .withMessage("Conditions should not contain one time use element.");
    }

    @Test
    public void shouldValidateNotOnOrAfterIfExists() {
        Instant notOnOrAfter = Instant.now();
        when(conditions.getNotOnOrAfter()).thenReturn(notOnOrAfter);

        validator.validate(conditions, "any-entity-id");

        verify(timeRestrictionValidator).validateNotOnOrAfter(notOnOrAfter);
    }

    @Test
    public void shouldNotValidateNotOnOrAfterIfExists() {
        Instant notOnOrAfter = null;
        when(conditions.getNotOnOrAfter()).thenReturn(notOnOrAfter);

        validator.validate(conditions, "any-entity-id");
    }

    @Test
    public void shouldValidateConditionsNotBefore() {
        Instant notBefore = Instant.now();
        when(conditions.getNotBefore()).thenReturn(notBefore);

        validator.validate(conditions, "any-entity-id");

        verify(timeRestrictionValidator).validateNotBefore(notBefore);
    }

    @Test
    public void shouldValidateConditionsAudienceRestrictions() {
        List<AudienceRestriction> audienceRestrictions = List.of(anAudienceRestriction().build());
        when(conditions.getAudienceRestrictions()).thenReturn(audienceRestrictions);

        validator.validate(conditions, "some-entity-id");

        verify(audienceRestrictionValidator).validate(audienceRestrictions, "some-entity-id");
    }
}