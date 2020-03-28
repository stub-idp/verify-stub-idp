package stubidp.saml.security.saml;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StringEncoding {

    private StringEncoding() {}

    public static String toBase64Encoded(String unencodedString) {
        return Base64.getMimeEncoder().encodeToString(unencodedString.getBytes(UTF_8));
    }

    public static String fromBase64Encoded(String encodedString) {
        return new String(Base64.getMimeDecoder().decode(encodedString), UTF_8);
    }
}
