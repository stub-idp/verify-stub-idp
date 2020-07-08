package certificates.values;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CACertificates {
    private static String readCertificateFile(String name) {
        try {
            return new String(CACertificates.class.getResourceAsStream("/ca-certificates/" + name).readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String TEST_ROOT_CA = readCertificateFile("ida-root-ca.pem.test");

    public static final String TEST_METADATA_CA = readCertificateFile("ida-metadata-ca.pem.test");

    public static final String TEST_IDP_CA = readCertificateFile("ida-intermediary-ca.pem.test");

    public static final String TEST_RP_CA = readCertificateFile("ida-intermediary-rp-ca.pem.test");

    public static final String TEST_CORE_CA = readCertificateFile("idap-core-ca.pem.test");
}
