package stubidp.saml.test.support;

import org.junit.jupiter.api.Assertions;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import static org.assertj.core.api.Assertions.assertThat;

public final class SamlTransformationErrorManagerTestHelper {

    private SamlTransformationErrorManagerTestHelper() {}

    public static void validateFail(Action action, SamlValidationSpecificationFailure failure) {
        final SamlTransformationErrorException e = Assertions.assertThrows(SamlTransformationErrorException.class, () -> action.execute());
        assertThat(e.getMessage()).isEqualTo(failure.getErrorMessage());
        assertThat(e.getLogLevel()).isEqualTo(failure.getLogLevel());
    }

    public interface Action {
        void execute();
    }
}
