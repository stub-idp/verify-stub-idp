package stubidp.saml.utils.hub.validators;

import org.junit.jupiter.api.Test;
import stubidp.saml.utils.OpenSAMLRunner;
import stubidp.saml.utils.core.test.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.utils.hub.errors.SamlTransformationErrorFactory;

public class StringSizeValidatorTest extends OpenSAMLRunner {

    @Test
    public void shouldPassIfStringSizeIsBetweenLowerAndUpperLimits() throws Exception {
        StringSizeValidator validator = new StringSizeValidator();

        String input = "This is between 10 and 30";

        validator.validate(input, 10, 30);
    }

    @Test
    public void shouldFailIfStringSizeIsLessThanLowerLimit() throws Exception {
        final StringSizeValidator validator = new StringSizeValidator();

        final String input = "Ring";

        SamlTransformationErrorManagerTestHelper.validateFail(
                new SamlTransformationErrorManagerTestHelper.Action() {
                    @Override
                    public void execute() {
                        validator.validate(input, 10, 30);
                    }
                },
                SamlTransformationErrorFactory.stringTooSmall(4, 10)
        );
    }

    @Test
    public void shouldFailIfStringSizeIsMoreThanUpperLimit() throws Exception {
        final StringSizeValidator validator = new StringSizeValidator();

        final String input = "Ring a ring";

        SamlTransformationErrorManagerTestHelper.validateFail(
                new SamlTransformationErrorManagerTestHelper.Action() {
                    @Override
                    public void execute() {
                        validator.validate(input, 0, 5);
                    }
                },
                SamlTransformationErrorFactory.stringTooLarge(11, 5)
        );
    }
}
