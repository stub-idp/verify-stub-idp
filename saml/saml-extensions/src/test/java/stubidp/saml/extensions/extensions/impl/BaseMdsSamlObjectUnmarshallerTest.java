package stubidp.saml.extensions.extensions.impl;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class BaseMdsSamlObjectUnmarshallerTest {
    @Test
    void testDateConversion() {
        assertThat(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1970-01-01")).isEqualTo(Instant.ofEpochSecond(0));
    }
}