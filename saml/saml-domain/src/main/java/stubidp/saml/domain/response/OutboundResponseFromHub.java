package stubidp.saml.domain.response;

import stubidp.saml.domain.assertions.TransactionIdaStatus;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public class OutboundResponseFromHub extends IdaSamlResponse {

    private final List<String> encryptedAssertions;
    private final TransactionIdaStatus status;

    public OutboundResponseFromHub(
            String responseId,
            String inResponseTo,
            String issuer,
            Instant issueInstant,
            TransactionIdaStatus status,
            List<String> encryptedAssertions,
            URI destination) {

        super(responseId, issueInstant, inResponseTo, issuer, destination);
        this.encryptedAssertions = encryptedAssertions;
        this.status = status;
    }

    public List<String> getEncryptedAssertions() {
        return encryptedAssertions;
    }

    public TransactionIdaStatus getStatus() {
        return status;
    }
}
