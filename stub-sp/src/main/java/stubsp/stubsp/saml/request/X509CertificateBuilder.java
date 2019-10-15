package stubsp.stubsp.saml.request;

import org.opensaml.xmlsec.signature.X509Certificate;

import java.util.Optional;

public class X509CertificateBuilder {

    private Optional<String> cert = Optional.empty();

    public static X509CertificateBuilder aX509Certificate() {
        return new X509CertificateBuilder();
    }

    public X509Certificate build() {
        X509Certificate x509Certificate = new org.opensaml.xmlsec.signature.impl.X509CertificateBuilder().buildObject();
        cert.ifPresent(x509Certificate::setValue);
        return x509Certificate;
    }

    public X509CertificateBuilder withCert(String cert) {
        this.cert = Optional.ofNullable(cert);
        return this;
    }
}
