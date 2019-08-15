package stubidp.test.devpki;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCertificateStringsTest {

    @Test
    public void shouldLoadKey() {
        assertThat(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY).isNotEmpty();
    }

    @Test
    public void shouldLoadCert() {
        assertThat(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT).isNotEmpty();
    }

}