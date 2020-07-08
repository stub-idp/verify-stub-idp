package stubidp.utils.security.security.exceptions;

import static java.text.MessageFormat.format;

public class CertificateLoadingException extends RuntimeException {
    public CertificateLoadingException(String certificate) {
        super(format("Unable to load certificate from {0}", certificate));
    }
}
