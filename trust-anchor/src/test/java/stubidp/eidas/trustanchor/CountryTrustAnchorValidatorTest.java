package stubidp.eidas.trustanchor;

import certificates.values.CACertificates;
import com.google.common.collect.ImmutableSet;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.List;

import static com.nimbusds.jose.JWSAlgorithm.RS256;
import static com.nimbusds.jose.jwk.KeyOperation.VERIFY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CountryTrustAnchorValidatorTest {

    @Mock
    private CertificateValidator mockValidator;

    private CountryTrustAnchorValidator testValidator;

    @BeforeEach
    public void setup() {
        testValidator = new CountryTrustAnchorValidator(mockValidator);
    }

    @Test
    public void validRSATrustAnchorShouldRaiseNoExceptions() {
        RSAKey validTrustAnchor = getValidRSATrustAnchor();
        Collection<String> errors = testValidator.findErrors(validTrustAnchor);

        assertThat(errors).isEmpty();
    }

    @Test
    public void validEC256TrustAnchorShouldRaiseNoExceptions() {
        ECKey validTrustAnchor = getValidECTrustAnchor(Curve.P_256);
        Collection<String> errors = testValidator.findErrors(validTrustAnchor);

        assertThat(errors).containsOnlyOnce("Expecting at least one X.509 certificate");
    }

    @Test
    public void validEC384TrustAnchorShouldRaiseNoExceptions() {
        ECKey validTrustAnchor = getValidECTrustAnchor(Curve.P_384);
        Collection<String> errors = testValidator.findErrors(validTrustAnchor);

        assertThat(errors).containsOnlyOnce("Expecting at least one X.509 certificate");
    }

    @Test
    public void validEC521TrustAnchorShouldRaiseNoExceptions() {
        ECKey validTrustAnchor = getValidECTrustAnchor(Curve.P_521);
        Collection<String> errors = testValidator.findErrors(validTrustAnchor);

        assertThat(errors).containsOnlyOnce("Expecting at least one X.509 certificate");
    }

    private RSAKey getValidRSATrustAnchor() {
        final String countryPublicCert = CACertificates.TEST_ROOT_CA
                .replace("-----BEGIN CERTIFICATE-----\n", "")
                .replace("\n-----END CERTIFICATE-----", "")
                .replace("\n", "")
                .trim();
        X509Certificate x509Certificate = new X509CertificateFactory().createCertificate(countryPublicCert);
        RSAPublicKey rsaPublicKey = (RSAPublicKey) x509Certificate.getPublicKey();

        return new RSAKey.Builder(rsaPublicKey)
                .keyID("TestId")
                .x509CertChain(List.of(new Base64(countryPublicCert)))
                .algorithm(RS256)
                .keyOperations(ImmutableSet.of(VERIFY))
                .build();
    }

    private ECKey getValidECTrustAnchor(Curve curve) {
        ECPublicKey publicKey = mock(ECPublicKey.class);
        when(publicKey.getW()).thenReturn(curve.toECParameterSpec().getGenerator());
        when(publicKey.getParams()).thenReturn(curve.toECParameterSpec());

        return new ECKey.Builder(curve, publicKey)
                .keyID("TestId")
                .x509CertChain(null)
                .algorithm(ECKeyHelper.getJWSAlgorithm(curve))
                .keyOperations(ImmutableSet.of(VERIFY))
                .build();
    }
}
