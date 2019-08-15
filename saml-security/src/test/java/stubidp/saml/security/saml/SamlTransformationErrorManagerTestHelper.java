package stubidp.saml.security.saml;

import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public final class SamlTransformationErrorManagerTestHelper {

    private SamlTransformationErrorManagerTestHelper() {
    }

    public static void validateFail(Action action, SamlValidationSpecificationFailure failure) {
        try {
            action.execute();
            fail("Expected action to throw");
        } catch (SamlTransformationErrorException e) {
            assertThat(e.getMessage()).isEqualTo(failure.getErrorMessage());
            assertThat(e.getLogLevel()).isEqualTo(failure.getLogLevel());
        }
    }

    public interface Action {
        void execute();
    }
}
