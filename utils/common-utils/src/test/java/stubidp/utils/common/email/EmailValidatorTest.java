package stubidp.utils.common.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailValidatorTest {

    @Test
    public void assertInternationalEmailIsValid() {
        assertThat(EmailValidator.isValid("björn.nußbaum@trouble.org")).isTrue();
    }

    @Test
    public void assertStandardEmailIsValid() {
        assertThat(EmailValidator.isValid("bjorn.nussbaum@trouble.org")).isTrue();
    }

    @Test
    public void assertInvalidEmail() {
        assertThat(EmailValidator.isValid("invalid")).isFalse();
    }
}
