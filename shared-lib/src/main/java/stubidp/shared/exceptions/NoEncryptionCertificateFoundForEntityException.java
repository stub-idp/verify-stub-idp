package stubidp.shared.exceptions;

import static java.text.MessageFormat.format;

public class NoEncryptionCertificateFoundForEntityException extends RuntimeException {
    public NoEncryptionCertificateFoundForEntityException(String expectedEntityId) {
        super(format("No encryption certificate found for {0}", expectedEntityId));
    }
}
