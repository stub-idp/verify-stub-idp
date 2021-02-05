package stubidp.saml.utils.hub.factories;

import org.apache.commons.codec.binary.Hex;
import org.opensaml.security.crypto.JCAConstants;
import stubidp.saml.domain.assertions.AuthnContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Optional;

public class UserIdHashFactory {
    private final String hashingEntityId;

    public UserIdHashFactory(String hashingEntityId) {
        this.hashingEntityId = hashingEntityId;
    }

    public String hashId(String issuerEntityId, String persistentId, Optional<AuthnContext> authnContext) {
        return UserIdHashFactory.hashId(this.hashingEntityId, issuerEntityId, persistentId, authnContext);
    }

    public static String hashId(String hashingEntityId, String issuerEntityId, String persistentId, Optional<AuthnContext> authnContext) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(JCAConstants.DIGEST_SHA256);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        final String toHash = idToHash(hashingEntityId, issuerEntityId, persistentId, authnContext);

        messageDigest.update(toHash.getBytes(StandardCharsets.UTF_8));

        byte[] digest = messageDigest.digest();
        return Hex.encodeHexString(digest);
    }

    private static String idToHash(String hashingEntityId, String issuerEntityId, String persistentId, Optional<AuthnContext> context) {
        String persistentIdHash;

        final AuthnContext authnContext = context.orElseThrow(() -> new AuthnContextMissingException(String.format("Authn context absent for persistent id %s", persistentId)));
        if (authnContext.equals(AuthnContext.LEVEL_2)) {
            // default behaviour - for LEVEL_2
            persistentIdHash = MessageFormat.format("{0}{1}{2}", issuerEntityId, hashingEntityId, persistentId);
        } else {
            // if we have an authnContext that is not LEVEL_2 then regenerate the hash
            // this does not break existing behaviour for LEVEL_2 RPs
            persistentIdHash = MessageFormat.format("{0}{1}{2}{3}", issuerEntityId, hashingEntityId, persistentId, authnContext.name());
        }
        return persistentIdHash;
    }

    static class AuthnContextMissingException extends RuntimeException {
        AuthnContextMissingException(String message) {
            super(message);
        }
    }
}
