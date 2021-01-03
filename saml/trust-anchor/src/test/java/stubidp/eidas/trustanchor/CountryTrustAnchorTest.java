package stubidp.eidas.trustanchor;

import certificates.values.CACertificates;
import com.nimbusds.jose.jwk.JWK;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CountryTrustAnchorTest {

    private final List<X509Certificate> invalidCertChain = createInvalidCertChain();

    @Test
    void makeThrowsExceptionIfIncludesInvalidCertificate() {
        final IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> CountryTrustAnchor.make(invalidCertChain, "key-id"));
        assertThat(e.getMessage()).startsWith("Managed to generate an invalid anchor: Certificate CN=IDA Stub Country Signing Dev");
    }

    @Test
    void makeValidationCanBeOverRidden() {
        JWK trustAnchorKey = CountryTrustAnchor.make(invalidCertChain, "key-id", false);

        assertThat(trustAnchorKey.getKeyID()).isEqualTo("key-id");
    }

    private List<X509Certificate> createInvalidCertChain() {
        List<String> certificates = asList(
            CACertificates.TEST_ROOT_CA,
            CACertificates.TEST_IDP_CA,
            TestCertificateStrings.STUB_COUNTRY_PUBLIC_NOT_YET_VALID_CERT
        );
        X509CertificateFactory certificateFactory = new X509CertificateFactory();
        return certificates.stream().map(certificateFactory::createCertificate).collect(Collectors.toList());
    }
}
