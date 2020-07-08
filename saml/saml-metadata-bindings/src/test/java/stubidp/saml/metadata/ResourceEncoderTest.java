package stubidp.saml.metadata;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceEncoderTest {
    @Test
    void test() {
        final String entity = "foo://foo";
        final String encoded = ResourceEncoder.entityIdAsResource(entity);
        assertThat(encoded.length()).isEqualTo(entity.length()*2);
        assertThat(encoded).isEqualTo("666f6f3a2f2f666f6f");
    }
}