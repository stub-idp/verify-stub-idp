package unit.uk.gov.ida.verifyserviceprovider.validators;

import org.junit.jupiter.api.Test;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LevelOfAssuranceValidatorTest {

    private static final LevelOfAssuranceValidator validator = new LevelOfAssuranceValidator();

    @Test
    public void shouldValidateThatTheLevelOfAssuranceExceedsTheOneExpected() {
        LevelOfAssurance levelOfAssurance = LevelOfAssurance.LEVEL_2;
        LevelOfAssurance expectedLevelOfAssurance = LevelOfAssurance.LEVEL_1;

        validator.validate(levelOfAssurance, expectedLevelOfAssurance);
    }

    @Test
    public void shouldThrowExceptionWhenLevelOfAssuranceIsLessThenExpected() {
        LevelOfAssurance levelOfAssurance = LevelOfAssurance.LEVEL_1;
        LevelOfAssurance expectedLevelOfAssurance = LevelOfAssurance.LEVEL_2;

        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> validator.validate(levelOfAssurance, expectedLevelOfAssurance))
                .withMessage(String.format(
                        "Expected Level of Assurance to be at least %s, but was %s",
                        expectedLevelOfAssurance,
                        levelOfAssurance
                ));
    }
}