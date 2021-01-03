package stubidp.test.devpki;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestCertificateStringsTest {

    @Test
    void shouldLoadKey() {
        assertThat(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY).isNotEmpty();
    }

    @Test
    void shouldLoadCert() {
        assertThat(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT).isNotEmpty();
    }

}