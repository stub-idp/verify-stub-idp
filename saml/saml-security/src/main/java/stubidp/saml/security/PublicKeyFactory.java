package stubidp.saml.security;

import org.opensaml.xmlsec.signature.X509Certificate;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;

// DUPLICATE CLASS
public class PublicKeyFactory {

    private final CertificateFactory certificateFactory;

    public PublicKeyFactory() throws CertificateException {
        certificateFactory = CertificateFactory.getInstance("X.509");
    }

    public PublicKey create(X509Certificate x509Certificate) {
        try {
            byte[] derValue = Base64.getMimeDecoder().decode(x509Certificate.getValue());
            Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(derValue));
            return certificate.getPublicKey();
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

}
