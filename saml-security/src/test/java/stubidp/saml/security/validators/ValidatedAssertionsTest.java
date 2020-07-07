package stubidp.saml.security.validators;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.test.builders.AttributeStatementBuilder;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidatedAssertionsTest extends OpenSAMLRunner {

    @Test
    public void should_returnMatchingDatasetAssertion() {
        Assertion mdsAssertion = AssertionBuilder.anAssertion().addAttributeStatement(AttributeStatementBuilder.anAttributeStatement().build()).buildUnencrypted();
        List<Assertion> assertions = asList(AssertionBuilder.anAssertion().buildUnencrypted(), mdsAssertion);

        ValidatedAssertions validatedAssertions = new ValidatedAssertions(assertions);

        assertThat(validatedAssertions.getMatchingDatasetAssertion().get()).isEqualTo(mdsAssertion);
    }

    @Test
    public void should_returnAuthnStatementAssertion() {
        Assertion mdsAssertion = AssertionBuilder.anAssertion().addAttributeStatement(AttributeStatementBuilder.anAttributeStatement().build()).buildUnencrypted();
        Assertion authnStatementAssertion = AssertionBuilder.anAssertion().buildUnencrypted();
        List<Assertion> assertions = asList(mdsAssertion, authnStatementAssertion);

        ValidatedAssertions validatedAssertions = new ValidatedAssertions(assertions);

        assertThat(validatedAssertions.getAuthnStatementAssertion().get()).isEqualTo(authnStatementAssertion);
    }

    @Test
    public void should_supportAnEmptyListOfAssertions() {
        ValidatedAssertions validatedAssertions = new ValidatedAssertions(emptyList());

        assertThat(validatedAssertions.getAuthnStatementAssertion().isPresent()).isFalse();
        assertThat(validatedAssertions.getMatchingDatasetAssertion().isPresent()).isFalse();
    }
}
