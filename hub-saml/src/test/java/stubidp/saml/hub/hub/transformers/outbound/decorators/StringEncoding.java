package stubidp.saml.hub.hub.transformers.outbound.decorators;

import java.util.Base64;

public class StringEncoding {
    private StringEncoding() {}

    public static String toBase64Encoded(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
