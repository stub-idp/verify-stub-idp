package stubidp.stubidp.security;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptHelperTest {

    @Test
    void checkIfPasswordCryptedAlready() {
        assertThat(BCryptHelper.alreadyCrypted(BCrypt.hashpw("foo", BCrypt.gensalt()))).isTrue();
        assertThat(BCryptHelper.alreadyCrypted("foo")).isFalse();
    }
}