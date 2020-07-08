package stubidp.utils.security.security.verification;

import java.security.cert.CertPathValidatorException;
import java.util.Optional;

public class CertificateValidity {
    private final Optional<CertPathValidatorException> exception;

    public static CertificateValidity valid() {
        return new CertificateValidity(Optional.<CertPathValidatorException>empty());
    }

    public static CertificateValidity invalid(CertPathValidatorException e) {
        return new CertificateValidity(Optional.ofNullable(e));
    }

    private CertificateValidity(Optional<CertPathValidatorException> exception) {
        this.exception = exception;
    }

    public boolean isValid() {
        return !exception.isPresent();
    }

    public Optional<CertPathValidatorException> getException() {
        return exception;
    }

}
