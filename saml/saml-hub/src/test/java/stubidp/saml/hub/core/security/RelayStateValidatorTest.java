package stubidp.saml.hub.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;

import static java.util.Arrays.asList;

public class RelayStateValidatorTest extends OpenSAMLRunner {

    private RelayStateValidator relayStateValidator;

    @BeforeEach
    void setUp() {
        relayStateValidator = new RelayStateValidator();
    }

    @Test
    void validate_shouldCheckRelayStateLengthIsLessThanEightyOneCharactersOrRaiseException() {
        final String aStringMoreThanEightyCharacters = generateLongString();
        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> relayStateValidator.validate(aStringMoreThanEightyCharacters),
                SamlTransformationErrorFactory.invalidRelayState(aStringMoreThanEightyCharacters)
        );
    }

    @Test
    void validate_shouldCheckRelayStateForValidStringAndNotThrowAnException() {
        String aStringLessThanEightyCharacters = generateShortString();

        relayStateValidator.validate(aStringLessThanEightyCharacters);
    }

    @Test
    void validate_shouldCheckForInvalidCharacters() {
        final String aString = "aStringWith";

        for (final String i : asList(">", "<", "'", "\"", "%", "&", ";")) {

            SamlTransformationErrorManagerTestHelper.validateFail(
                    () -> relayStateValidator.validate(aString + i),
                    SamlTransformationErrorFactory.relayStateContainsInvalidCharacter(i, aString + i)
            );
        }
    }

    private String generateShortString() {
        return "short string";
    }

    private String generateLongString() {
        return "a".repeat(82);
    }
}
