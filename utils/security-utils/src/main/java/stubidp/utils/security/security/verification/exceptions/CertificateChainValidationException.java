package stubidp.utils.security.security.verification.exceptions;

public class CertificateChainValidationException extends RuntimeException {
    public CertificateChainValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
