package stubidp.utils.common.string;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StringEncoding {

    private StringEncoding() {}

    private static final String ENCODING = "UTF-8";

    public static String toBase64Encoded(String unencodedString) {
        return Base64.getMimeEncoder().encodeToString(unencodedString.getBytes(UTF_8));
    }

    public static String toBase64Encoded(byte[] bytes) {
        return Base64.getMimeEncoder().encodeToString(bytes);
    }

    public static String fromBase64Encoded(String encodedString) {
        return new String(Base64.getMimeDecoder().decode(encodedString), UTF_8);
    }

    public static byte[] fromBase64ToByteArrayEncoded(String encodedString) {
        return Base64.getMimeDecoder().decode(encodedString);
    }

    public static String urlEncode(String input) {
        String encodedValue;
        try {
            encodedValue = URLEncoder.encode(input, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return encodedValue;
    }

    private static String urlDecode(String input) {
        String decodedValue;
        try {
            decodedValue = URLDecoder.decode(input, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return decodedValue;
    }

    public static String nullSafeUrlDecode(String urlEncodedValue) {
        String urlDecodedValue = null;
        if (urlEncodedValue != null) {
            urlDecodedValue = urlDecode(urlEncodedValue);
        }
        return urlDecodedValue;
    }
}
