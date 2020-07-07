package stubidp.saml.test.support;

import java.util.UUID;

public final class AuthnRequestIdGenerator {
    private AuthnRequestIdGenerator() {}

    public static String generateRequestId() {
        return "_" + UUID.randomUUID().toString();
    }
}
