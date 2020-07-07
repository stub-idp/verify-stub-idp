package stubidp.saml.test;

import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.util.Base64;

public class TestCredentialFactory {
    private final String publicCert;
    private final String privateKey;

    public TestCredentialFactory(String publicCert, String privateKey) {
        this.publicCert = publicCert;
        this.privateKey = privateKey;
    }

    public Credential getSigningCredential() {
        BasicCredential credential = new BasicCredential(getPublicKey(), getPrivateKey());

        credential.setUsageType(UsageType.SIGNING);
        return credential;
    }

    public Credential getEncryptingCredential() {
        BasicCredential credential = new BasicCredential(getPublicKey());

        credential.setUsageType(UsageType.ENCRYPTION);
        return credential;
    }

    public Credential getDecryptingCredential() {
        BasicCredential credential = new BasicCredential(getPublicKey(), getPrivateKey());

        credential.setUsageType(UsageType.ENCRYPTION);
        return credential;
    }

    private PublicKey getPublicKey() {
        try {
            return createPublicKey(publicCert);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private PrivateKey getPrivateKey() {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(privateKey));
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey createPublicKey(String partialCert) throws CertificateException {
        CertificateFactory certificateFactory;
        certificateFactory = CertificateFactory.getInstance("X.509");
        String fullCert;
        if (partialCert.contains("-----BEGIN CERTIFICATE-----")) {
            fullCert = partialCert;
        } else {
            fullCert = MessageFormat.format("-----BEGIN CERTIFICATE-----\n{0}\n-----END CERTIFICATE-----", partialCert.trim());
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fullCert.getBytes(StandardCharsets.UTF_8));
        Certificate certificate = certificateFactory.generateCertificate(byteArrayInputStream);
        return certificate.getPublicKey();
    }
}
