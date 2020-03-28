package stubidp.utils.security.security;

import java.util.Base64;

class StringEncoding {

    private StringEncoding() {}

    public static String toBase64Encoded(byte[] bytes) {
        return Base64.getMimeEncoder().encodeToString(bytes);
    }
}
