package uk.gov.ida.matchingserviceadapter;

import org.junit.jupiter.api.Test;
import stubidp.utils.security.security.Certificate;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingServiceAdapterBinderTest {

    private static final String CERT = "-----BEGIN CERTIFICATE-----\nMIIBGzCBxgIJAL0noY5tc8OPMA0GCSqGSIb3DQEBCwUAMBUxEzARBgNVBAMMCnNl\nbGZzaWduZWQwHhcNMTgwODIzMDY1MzM2WhcNMTkwODIzMDY1MzM2WjAVMRMwEQYD\nVQQDDApzZWxmc2lnbmVkMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMS856cUwkeE\nrqtE+IyfzSFHECkKsOw35xQTNo3u32IjbwzykzOC2x+Pvyh47U3DXM52wPzi3uiL\n+GB4WOtEL0cCAwEAATANBgkqhkiG9w0BAQsFAANBAIIrGyaQCLIqCutaICJbdbIN\nmUzVkrY1iFLRVrfSZ37Ush1sxqpr/YHRf+apHMRXHlITuBrU8HIZbYEiaJUP718=\n-----END CERTIFICATE-----";

    @Test
    void testCertLoading() {
        MatchingServiceAdapterBinder matchingServiceAdapterBinder = new MatchingServiceAdapterBinder(null, null, null);
        final Certificate certificate = matchingServiceAdapterBinder.cert("test cert", CERT, Certificate.KeyUse.Signing);
        assertThat(certificate.getCertificate()).isEqualTo("MIIBGzCBxgIJAL0noY5tc8OPMA0GCSqGSIb3DQEBCwUAMBUxEzARBgNVBAMMCnNlbGZzaWduZWQwHhcNMTgwODIzMDY1MzM2WhcNMTkwODIzMDY1MzM2WjAVMRMwEQYDVQQDDApzZWxmc2lnbmVkMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMS856cUwkeErqtE+IyfzSFHECkKsOw35xQTNo3u32IjbwzykzOC2x+Pvyh47U3DXM52wPzi3uiL+GB4WOtEL0cCAwEAATANBgkqhkiG9w0BAQsFAANBAIIrGyaQCLIqCutaICJbdbINmUzVkrY1iFLRVrfSZ37Ush1sxqpr/YHRf+apHMRXHlITuBrU8HIZbYEiaJUP718=");

        final Certificate certificateWithText = matchingServiceAdapterBinder.cert("test cert", "Some text before the cert\n" + CERT, Certificate.KeyUse.Signing);
        assertThat(certificateWithText.getCertificate()).isEqualTo("MIIBGzCBxgIJAL0noY5tc8OPMA0GCSqGSIb3DQEBCwUAMBUxEzARBgNVBAMMCnNlbGZzaWduZWQwHhcNMTgwODIzMDY1MzM2WhcNMTkwODIzMDY1MzM2WjAVMRMwEQYDVQQDDApzZWxmc2lnbmVkMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMS856cUwkeErqtE+IyfzSFHECkKsOw35xQTNo3u32IjbwzykzOC2x+Pvyh47U3DXM52wPzi3uiL+GB4WOtEL0cCAwEAATANBgkqhkiG9w0BAQsFAANBAIIrGyaQCLIqCutaICJbdbINmUzVkrY1iFLRVrfSZ37Ush1sxqpr/YHRf+apHMRXHlITuBrU8HIZbYEiaJUP718=");
    }
}
