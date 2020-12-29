package stubidp.saml.hub.core.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.test.OpenSAMLRunner;

import java.net.URI;
import java.net.URISyntaxException;

import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.destinationEmpty;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.destinationMissing;
import static stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper.validateFail;

public class DestinationValidatorTest extends OpenSAMLRunner {
    private static final String EXPECTED_DESTINATION = "http://correct.destination.com";
    private static final String EXPECTED_ENDPOINT = "/foo/bar";

    private DestinationValidator validator;

    @BeforeEach
    public void setup() {
        validator = new DestinationValidator(URI.create(EXPECTED_DESTINATION), EXPECTED_ENDPOINT);
    }

    @Test
    public void validate_shouldThrowExceptionIfDestinationIsAbsent() {
        validateException(
            destinationMissing(URI.create(EXPECTED_DESTINATION + EXPECTED_ENDPOINT)),
            null
        );
    }

    @Test
    public void validate_shouldNotThrowExceptionIfUriMatches() {
        validator.validate("http://correct.destination.com/foo/bar");
    }

    @Test
    public void validate_shouldBeValidIfPortSpecifiedOnDestinationButNotForSamlProxy() {
        validator.validate("http://correct.destination.com:999/foo/bar");
    }

    @Test
    public void validate_shouldThrowSamlExceptionIfHostForTheUriOnResponseDoesNotMatchTheSamlReceiverHost() {
        String invalidDestination = "http://saml.com/foo/bar";
        validateException(
            destinationEmpty(URI.create(EXPECTED_DESTINATION + EXPECTED_ENDPOINT), invalidDestination),
            invalidDestination
        );
    }

    @Test
    public void validate_shouldThrowSamlExceptionIfHostsMatchButPathsDoNot() {
        validateException(
            destinationEmpty(URI.create(EXPECTED_DESTINATION + EXPECTED_ENDPOINT), EXPECTED_DESTINATION + "/this/is/a/path"),
            EXPECTED_DESTINATION + "/this/is/a/path"
        );
    }

    private void validateException(SamlValidationSpecificationFailure failure, final String destination) {
        validateFail(() -> validator.validate(destination), failure);
    }
}
