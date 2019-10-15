package stubsp.stubsp.saml.request;

import java.util.UUID;

final class AuthnRequestIdGenerator {
    private AuthnRequestIdGenerator() {}

    static String generateRequestId() {
        return "_" + UUID.randomUUID().toString();
    }
}
