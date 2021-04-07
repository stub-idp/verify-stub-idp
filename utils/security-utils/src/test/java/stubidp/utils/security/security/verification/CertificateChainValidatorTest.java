package stubidp.utils.security.security.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import stubidp.utils.security.security.X509CertificateFactory;
import stubidp.utils.security.security.verification.CertificateChainValidator;
import stubidp.utils.security.security.verification.CertificateValidity;
import stubidp.utils.security.security.verification.OCSPPKIXParametersProvider;
import stubidp.utils.security.security.verification.PKIXParametersProvider;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertificateException;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;

public class CertificateChainValidatorTest {
    private X509CertificateFactory x509CertificateFactory;
    private CertificateChainValidator certificateChainValidator;

    @BeforeEach
    void setUp() {
        x509CertificateFactory = new X509CertificateFactory();
        certificateChainValidator = new CertificateChainValidator(new PKIXParametersProvider(), x509CertificateFactory);
    }

    @Test
    void validate_shouldReturnValidForACertSignedByRootCACertInTrustStore() throws Exception {
        final X509Certificate intermediaryCACertificate = x509CertificateFactory.createCertificate(caCert);
        final CertificateValidity validate = certificateChainValidator.validate(intermediaryCACertificate, getTrustStore());

        assertThat(validate.isValid()).isTrue();
    }

    @Test
    void validate_shouldReturnValidForACertSignedByAnIntermediaryCACertSignedByRootCACertInTrustStore() throws Exception {
        final X509Certificate encryptionCertificate = x509CertificateFactory.createCertificate(this.trustedCertString);
        final CertificateValidity validate = certificateChainValidator.validate(encryptionCertificate, getTrustStore());

        assertThat(validate.isValid()).isTrue();
    }

    @Test
    void validate_shouldReturnInvalidForACertSignedByAnUnknownRootCACert() throws Exception {
        final X509Certificate otherChildCertificate = x509CertificateFactory.createCertificate(childSignedByOtherRootCAString);
        CertificateValidity certificateValidity = certificateChainValidator.validate(otherChildCertificate, getTrustStore());

        assertThat(certificateValidity.isValid()).isEqualTo(false);
        CertPathValidatorException exception = certificateValidity.getException().get();
        assertThat(exception.getReason()).isEqualTo(PKIXReason.NO_TRUST_ANCHOR);
        assertThat(exception.getMessage()).isEqualTo("Path does not chain with any of the trust anchors");
    }

    @Test
    void validate_shouldReturnValidForAValidCertWhenPassedInAString() throws Exception {
        CertificateValidity certificateValidity = certificateChainValidator.validate(caCert, getTrustStore());

        assertThat(certificateValidity.isValid()).isTrue();
    }

    @Test
    void validate_shouldReturnInvalidForInvalidCertificateWhenPassedAString() throws Exception {
        CertificateValidity certificateValidity = certificateChainValidator.validate(childSignedByOtherRootCAString, getTrustStore());

        assertThat(certificateValidity.isValid()).isEqualTo(false);
        CertPathValidatorException exception = certificateValidity.getException().get();
        assertThat(exception.getReason()).isEqualTo(PKIXReason.NO_TRUST_ANCHOR);
        assertThat(exception.getMessage()).isEqualTo("Path does not chain with any of the trust anchors");
    }

    @Test
    void validate_shouldReturnInvalidForExpiredCertificate() throws Exception {
        CertificateValidity certificateValidity = certificateChainValidator.validate(expiredCertString, getTrustStore());

        assertThat(certificateValidity.isValid()).isEqualTo(false);
        CertPathValidatorException exception = certificateValidity.getException().get();
        assertThat(exception.getReason()).isEqualTo(BasicReason.EXPIRED);
    }

    @Test
    @Disabled("This test often fails due to the OCSP server not being available. Card #1946 (and possibly others) are raised to investigate")
    void should_doAnOcspCheck() throws Exception {
        final X509Certificate encryptionCertificate = x509CertificateFactory.createCertificate(trustedCertString);
        CertificateChainValidator chainValidator = new CertificateChainValidator(new OCSPPKIXParametersProvider(), x509CertificateFactory);
        CertificateValidity validate = chainValidator.validate(encryptionCertificate, getTrustStore());

        assertThat(validate.isValid()).isEqualTo(true);
    }

    KeyStore getTrustStore() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        X509Certificate certificate = new X509CertificateFactory().createCertificate(caCert);
        ks.setCertificateEntry("CA", certificate);
        return ks;
    }

    private final String caCert = """
            -----BEGIN CERTIFICATE-----
            MIICyDCCAbCgAwIBAgIJAJmsCyNdn2hMMA0GCSqGSIb3DQEBBQUAMBMxETAPBgNV
            BAMMCE15VGVzdENBMCAXDTE1MDgxNzA4MzgzMFoYDzIxMTUwNzI0MDgzODMwWjAT
            MREwDwYDVQQDDAhNeVRlc3RDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC
            ggEBALtR8OLWpW9aKh5GYVmyBo+ZGr4AjyhFlf7pa9rhG9K6fvNnBllUbcQhBv+r
            34YwIZ50mWKVuD5Jlb8Ad6506vWq1EX1WXUIB36ZdCRx26zUquAtYi448ULKoN4k
            qVRpsmsfguBC/u7EvWoD+ImsKiGwm2+Q0d87z0ImkkVTf6100MMyR799WSEaqXuS
            sW1CG3Oerhnj4FvMgs1ISX+5jdZjwzvkKIxGWPtKTuXfcXKCrV/+spuQFffcYRkG
            emtxJPoaM/BuA7sDzwP0ASZNr6vtFoeQGhCk7q+qjTKkRzQB949F0O2K3KqOlCI0
            x5J/VnRiIPVCbJJZtYv6DO+Y15ECAwEAAaMdMBswDAYDVR0TBAUwAwEB/zALBgNV
            HQ8EBAMCAQYwDQYJKoZIhvcNAQEFBQADggEBAFvp1PSiDfVbFbTdX1Zil35vyq5t
            VmEJyRF2hHOY+RSpJqezDoP7SuDtf3Ui7lS+yzzt3W/ZbsqOHJdMrDT7y7yWyEh8
            hJGYo7MI2Ih9+nlsF8mw6G/vxs7wERTEs5OGpVZeIEW3crSlSLObUVrIwuHJxPOR
            ovDT3w6hVCE0hzbu+4y38TRTyhVgLj0cFyCIhs52BOCuB2JjeW88UQCKp65wHZBk
            Ne0xXBtsfhFc1dW3Mnvix89nrv6cVCxnqO3lMOQGzP11XXrMYJmvdRKD8/z10HnO
            Om7P4NDY2IIGbaPzlJlbmZgVKWg0nBXSOjAEA6Q9JCri+rjbZEJGldUyTPI=
            -----END CERTIFICATE-----
            """;


    private final String trustedCertString = """
            -----BEGIN CERTIFICATE-----
            MIIC5DCCAcygAwIBAgIBBTANBgkqhkiG9w0BAQsFADATMREwDwYDVQQDDAhNeVRl
            c3RDQTAgFw0xNTA4MTcxMTE3MDBaGA8yMTE1MDcyNDExMTcwMFowJTESMBAGA1UE
            AxMJbG9jYWxob3N0MQ8wDQYDVQQKEwZzZXJ2ZXIwggEiMA0GCSqGSIb3DQEBAQUA
            A4IBDwAwggEKAoIBAQDCjm8RzlK7XneBfDmrqsnqGcu7yA5YgF73HEFX0FHghyb7
            hX9Hy9C11sXVC5a1Bu0kv60Jnl5/WgZg3+CvQAubIEtQF7CcWuBqa2f2R15cUPB4
            QAWgQ5rGJkGY+RTB1cgsILog1K0R8wDocTqxqdQrO35oOSFwtU6uk2PurSKnKMVQ
            APD/3mIG3U3yYAJUi8OpM25ICgrxOmTZ1C0nY/tM5CHLTd73UuJ1IEljYq7PYfdy
            IWaYJCQdl0rnwOyUGti7kpmaLB4ypLZETz2gQFF6s8D8O2ed1pLaeR1Qns9av2Vl
            i1mkfweJlS53adUekqhcYA6EoJv6BqNFMRk4WRBFAgMBAAGjLzAtMAkGA1UdEwQC
            MAAwCwYDVR0PBAQDAgWgMBMGA1UdJQQMMAoGCCsGAQUFBwMBMA0GCSqGSIb3DQEB
            CwUAA4IBAQAUfrhCQxfX2Gzd5Oa2weNpviDEyouConVVHx+3dJvLkh1kCFXBJgoe
            kt7BeV41HcXv7KU1I+v/9admXlTVndKyO+AOOvfj75A0x6BBcRF2hMtnANNP3voW
            CVQTTAwFJtddCoqM/wAf9AieO0I9rtKHDSjuZ1LL9Tb1qGlsKqnOyr0I/bkVLOfp
            BDFfenS08kJqhFfV7e56yYhc1fM26CmzXBfvZFjcHMBMWzISZ+ss8/q1RVFEGkdi
            Ejp/JdzKioT+3Ctkd4taI4ccdRA2jCWUiFXEXRhdvxcLlX8XwqxEbKT5nYASva0P
            bIBwAaTnGn53cXxMuHz1KxHcdbxOFkaf
            -----END CERTIFICATE-----
            """;


    private final String expiredCertString = """
            -----BEGIN CERTIFICATE-----
            MIIC+TCCAeGgAwIBAgIBCzANBgkqhkiG9w0BAQsFADATMREwDwYDVQQDDAhNeVRl
            c3RDQTAeFw0xNTAxMDEwMDAwMDFaFw0xNTAxMDIwMDAwMDFaMDwxHTAbBgNVBAMT
            FGV4cGlyZWQgY2VydGlmaWNpYXRlMRswGQYDVQQKExJFWFBJUkVEQ0VSVElGSUNB
            VEUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDCjm8RzlK7XneBfDmr
            qsnqGcu7yA5YgF73HEFX0FHghyb7hX9Hy9C11sXVC5a1Bu0kv60Jnl5/WgZg3+Cv
            QAubIEtQF7CcWuBqa2f2R15cUPB4QAWgQ5rGJkGY+RTB1cgsILog1K0R8wDocTqx
            qdQrO35oOSFwtU6uk2PurSKnKMVQAPD/3mIG3U3yYAJUi8OpM25ICgrxOmTZ1C0n
            Y/tM5CHLTd73UuJ1IEljYq7PYfdyIWaYJCQdl0rnwOyUGti7kpmaLB4ypLZETz2g
            QFF6s8D8O2ed1pLaeR1Qns9av2Vli1mkfweJlS53adUekqhcYA6EoJv6BqNFMRk4
            WRBFAgMBAAGjLzAtMAkGA1UdEwQCMAAwCwYDVR0PBAQDAgWgMBMGA1UdJQQMMAoG
            CCsGAQUFBwMBMA0GCSqGSIb3DQEBCwUAA4IBAQAqt2ZPp0Kvy3j5DOqzBxpHRSuX
            sUqxx9yDZCwoktxxHj4cQFb1zkAs3qqgQ2yiuD+toq7yfemBlRC2H6n7X0zQPC4I
            YTzJ9ALWD/31wkIlj/xvOzytpSXEh37rWqtDv0FDdgmdXHdmLSlwVSqmGTeBKuev
            eskk8gWkyBc+3DTklkhg+DZAk0revybBUN4UrEemO2Y/7FBCMUvr+nh/N4fZnBi/
            qhqY1SrKldRBWpt+p9Opw+iqO7qhtknW6l2D2J2w8BQNh8+zOunQvVaknuMrRT4P
            cqjerViuj0BkPz3aRCvGrda734Xcki8IZS5TnEPQt4LSmX8AN39lLaTOXFHq
            -----END CERTIFICATE-----
            """;

    private final String childSignedByOtherRootCAString = """
            -----BEGIN CERTIFICATE-----
            MIIC7TCCAdWgAwIBAgIBAjANBgkqhkiG9w0BAQUFADAWMRQwEgYDVQQDDAtPdGhl
            clRlc3RDQTAgFw0xNTA5MTUxMTE1NDVaGA8yMTE1MDgyMjExMTU0NVowKzESMBAG
            A1UEAxMJbG9jYWxob3N0MRUwEwYDVQQKFAxvdGhlcl9zZXJ2ZXIwggEiMA0GCSqG
            SIb3DQEBAQUAA4IBDwAwggEKAoIBAQDCjm8RzlK7XneBfDmrqsnqGcu7yA5YgF73
            HEFX0FHghyb7hX9Hy9C11sXVC5a1Bu0kv60Jnl5/WgZg3+CvQAubIEtQF7CcWuBq
            a2f2R15cUPB4QAWgQ5rGJkGY+RTB1cgsILog1K0R8wDocTqxqdQrO35oOSFwtU6u
            k2PurSKnKMVQAPD/3mIG3U3yYAJUi8OpM25ICgrxOmTZ1C0nY/tM5CHLTd73UuJ1
            IEljYq7PYfdyIWaYJCQdl0rnwOyUGti7kpmaLB4ypLZETz2gQFF6s8D8O2ed1pLa
            eR1Qns9av2Vli1mkfweJlS53adUekqhcYA6EoJv6BqNFMRk4WRBFAgMBAAGjLzAt
            MAkGA1UdEwQCMAAwCwYDVR0PBAQDAgWgMBMGA1UdJQQMMAoGCCsGAQUFBwMBMA0G
            CSqGSIb3DQEBBQUAA4IBAQCGfTcqLiqr4fmm/iHbvT+Qgr1YjN9td9BK30D031Rb
            LItxSR5dAgC+L9A3n/o1wWszqF2kxl9AMRfQfIILNflJ4ED4ymrsAtN2VdWlhv7y
            io4LT49CFc7BF+EYj07zT6zu0gDCLBHbH3Ah1Iack82UHqZfjMKB4pw0FeoKOJ72
            kNji93SKj6jh5hPQXd678+I2GQvqLzZ6FGQ681O6FMVknDDkXuKdC49VTsw13iK7
            3OPERq/ptqpZIeYJ9yeGYGdKXZpnA8kElSrXIISvjNZrGAbXmwtn5437m+4O6Wpy
            2rx8g1cBTmbLe2MBPY3pvPAXJ33Tk6fwtNqD4Eh6mS1Q
            -----END CERTIFICATE-----
            """;
}
