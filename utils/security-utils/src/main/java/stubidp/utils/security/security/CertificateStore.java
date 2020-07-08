package stubidp.utils.security.security;

import stubidp.utils.security.configuration.DeserializablePublicKeyConfiguration;

import java.util.ArrayList;
import java.util.List;

public class CertificateStore {

    private final List<DeserializablePublicKeyConfiguration> publicEncryptionKeyConfigurations;
    private final List<DeserializablePublicKeyConfiguration> publicSigningKeyConfigurations;

    public CertificateStore(
            List<DeserializablePublicKeyConfiguration> publicEncryptionKeyConfigurations,
            List<DeserializablePublicKeyConfiguration> publicSigningKeyConfiguration) {

        this.publicEncryptionKeyConfigurations = publicEncryptionKeyConfigurations;
        this.publicSigningKeyConfigurations = publicSigningKeyConfiguration;
    }

    public List<Certificate> getEncryptionCertificates() {
        List<Certificate> certs = new ArrayList<>();
        for (DeserializablePublicKeyConfiguration certConfig : publicEncryptionKeyConfigurations) {
            certs.add(new Certificate(certConfig.getName(), stripHeaders(certConfig.getCert()), Certificate.KeyUse.Encryption));
        }
        return certs;
    }

    public List<Certificate> getSigningCertificates() {
        List<Certificate> certs = new ArrayList<>();
        for (DeserializablePublicKeyConfiguration certConfig : publicSigningKeyConfigurations) {
            certs.add(new Certificate(certConfig.getName(), stripHeaders(certConfig.getCert()), Certificate.KeyUse.Signing));
        }
        return certs;
    }

    private String stripHeaders(final String originalCertificate) {
        String strippedCertificate = originalCertificate;
        if (originalCertificate.contains(Certificate.BEGIN_CERT)){
            strippedCertificate = originalCertificate.replace(Certificate.BEGIN_CERT, "");
        }
        if (originalCertificate.contains(Certificate.END_CERT)){
            strippedCertificate = strippedCertificate.replace(Certificate.END_CERT, "");
        }
        return strippedCertificate.replace(" ","");
    }
}
