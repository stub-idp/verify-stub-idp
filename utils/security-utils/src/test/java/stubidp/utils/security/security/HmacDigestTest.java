package stubidp.utils.security.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.security.configuration.SecureCookieKeyStore;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HmacDigestTest {

    @Mock
    private HmacDigest.HmacSha256MacFactory macFactory;

    @Mock
    private SecureCookieKeyStore secureCookieKeyStore;

    private final String key = "this-is-my-secret-key";
    private final Key secretKey = new SecretKeySpec("this-is-my-secret-key".getBytes(), "HmacSHA256");

    @Test
    public void digest_shouldDDigestAValueUsingHmac() {
        //expected hmac generated on the shell using:
        //echo -n 'string to be encoded' | openssl dgst -sha256 -hmac 'this-is-my-secret-key' | xxd -r -p | base64
        final String expectedHMAC = "Twk5x/dX6Dh/2m1S6PC6uS4V//JTeU56oW2sm1CSZ8g=";

        when(secureCookieKeyStore.getKey()).thenReturn(secretKey);
        HmacDigest digest = new HmacDigest(new HmacDigest.HmacSha256MacFactory(), secureCookieKeyStore);
        String result = digest.digest("string to be encoded");
        assertThat(result).isEqualTo(expectedHMAC);
    }
}
