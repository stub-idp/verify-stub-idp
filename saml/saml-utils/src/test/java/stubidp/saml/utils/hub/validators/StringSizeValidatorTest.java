package stubidp.saml.utils.hub.validators;

import org.junit.jupiter.api.Test;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.utils.hub.errors.SamlTransformationErrorFactory;

public class StringSizeValidatorTest extends OpenSAMLRunner {

    @Test
    void shouldPassIfStringSizeIsBetweenLowerAndUpperLimits() {
        StringSizeValidator validator = new StringSizeValidator();

        String input = "This is between 10 and 30";

        validator.validate(input, 10, 30);
    }

    @Test
    void shouldFailIfStringSizeIsLessThanLowerLimit() {
        final StringSizeValidator validator = new StringSizeValidator();

        final String input = "Ring";

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(input, 10, 30),
                SamlTransformationErrorFactory.stringTooSmall(4, 10)
        );
    }

    @Test
    void shouldFailIfStringSizeIsMoreThanUpperLimit() {
        final StringSizeValidator validator = new StringSizeValidator();

        final String input = "Ring a ring";

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(input, 0, 5),
                SamlTransformationErrorFactory.stringTooLarge(11, 5)
        );
    }
}
