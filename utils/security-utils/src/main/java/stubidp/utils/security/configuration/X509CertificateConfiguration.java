package stubidp.utils.security.configuration;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.utils.security.security.Certificate;

import static java.text.MessageFormat.format;

public class X509CertificateConfiguration extends DeserializablePublicKeyConfiguration {
    @JsonCreator
    public X509CertificateConfiguration(@JsonProperty("cert") @JsonAlias({ "x509" }) String cert) {
        this.fullCertificate = format("{0}\n{1}\n{2}", Certificate.BEGIN_CERT, cert.trim(), Certificate.END_CERT);
        this.certificate = getCertificateFromString(fullCertificate);
    }
}