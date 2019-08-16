package stubidp.eventemitter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Sha256UtilTest {

    private static final Sha256Util SHA_256_UTIL = new Sha256Util();

    @Test
    public void shouldHashOneString() {
        final String expectedResult = "dc1c54dbf11201b9df33fb666cce7586d73633d6fe01d7dbff65fd37c8cebfcf";

        final String actualResult = SHA_256_UTIL.hash("some strings");

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldHashTwoStrings() {
        final String expectedResult = "69fc1417e88e916fe45a7e5da0f9be95e8a90d260b52c06ab89931698b58d634";

        final String actualResult = SHA_256_UTIL.hash("some", "strings");

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenHashingEmptyArray() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SHA_256_UTIL.hash());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenHashingNull() {
        final String[] nullArray = null;
        Assertions.assertThrows(IllegalArgumentException.class, () -> SHA_256_UTIL.hash(nullArray));
    }
}

