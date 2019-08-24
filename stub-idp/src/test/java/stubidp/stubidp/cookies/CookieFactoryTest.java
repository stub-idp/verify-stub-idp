package stubidp.stubidp.cookies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.rest.common.SessionId;
import stubidp.utils.security.configuration.SecureCookieConfiguration;
import stubidp.utils.security.security.HmacDigest;

import javax.ws.rs.core.NewCookie;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CookieFactoryTest {

    private final int SESSION_COOKIE_MAX_AGE = -1;
    private final boolean IS_SECURE = true;

    @Mock
    HmacDigest hmacDigest;

    @Mock
    SecureCookieConfiguration secureCookieConfiguration;

    CookieFactory cookieFactory;

    @BeforeEach
    public void setup() {
        cookieFactory = new CookieFactory(hmacDigest, secureCookieConfiguration);
        when(secureCookieConfiguration.isSecure()).thenReturn(IS_SECURE);
    }

    @Test
    public void createSecureCookieWithSecurelyHashedValue_shouldCreateACookie() throws Exception {
        SessionId sessionId = new SessionId(UUID.randomUUID().toString());

        String digestedValue = "ummm cookie";
        when(hmacDigest.digest(sessionId.toString())).thenReturn(digestedValue);


        NewCookie cookie = cookieFactory.createSecureCookieWithSecurelyHashedValue(sessionId);

        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo(digestedValue);
        assertThat(cookie.getMaxAge()).isEqualTo(SESSION_COOKIE_MAX_AGE);
        assertThat(cookie.isSecure()).isEqualTo(IS_SECURE);
        assertIsHttpOnly(cookie);
    }

    @Test
    public void createSessionIdCookie_shouldCreateACookie() throws Exception {

        when(secureCookieConfiguration.isSecure()).thenReturn(IS_SECURE);
        SessionId expectedValue = SessionId.createNewSessionId();

        NewCookie cookie = cookieFactory.createSessionIdCookie(expectedValue);

        assertThat(cookie.getName()).isEqualTo(CookieNames.SESSION_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(expectedValue.toString());
        assertThat(cookie.getMaxAge()).isEqualTo(SESSION_COOKIE_MAX_AGE);
        assertThat(cookie.isSecure()).isEqualTo(IS_SECURE);
        assertIsHttpOnly(cookie);
    }

    private void assertIsHttpOnly(NewCookie deletedSecureCookie) {
        assertThat(deletedSecureCookie.toString()).contains(" HttpOnly");
    }
}
