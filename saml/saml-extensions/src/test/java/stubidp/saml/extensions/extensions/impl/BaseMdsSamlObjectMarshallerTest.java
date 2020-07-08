package stubidp.saml.extensions.extensions.impl;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class BaseMdsSamlObjectMarshallerTest {
    @Test
    void testDateConversion() {
        assertThat(BaseMdsSamlObjectMarshaller.DateFromInstant.of(Instant.ofEpochSecond(0))).isEqualTo("1970-01-01");
    }
}