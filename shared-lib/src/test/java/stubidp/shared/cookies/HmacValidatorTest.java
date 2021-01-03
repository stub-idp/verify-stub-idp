package stubidp.shared.cookies;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.rest.common.SessionId;
import stubidp.utils.security.security.HmacDigest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HmacValidatorTest {

    @Mock
    private HmacDigest hmacDigest;

    @Test
    void shouldReturnTrueWhenSessionCookieAndSecureCookieMatchUp() {
        SessionId sessionId = SessionId.createNewSessionId();
        String secureCookie = "secure-cookie";
        String hmacDouble = "hmac-double";
        when(hmacDigest.digest(sessionId.getSessionId())).thenReturn("session-id-hmac");
        when(hmacDigest.digest("session-id-hmac")).thenReturn(hmacDouble);
        when(hmacDigest.digest(secureCookie)).thenReturn(hmacDouble);
        HmacValidator hmacValidator = new HmacValidator(hmacDigest);
        assertThat(hmacValidator.validateHMACSHA256(secureCookie, sessionId.getSessionId())).isEqualTo(true);
    }

    @Test
    void shouldReturnFalseWhenSessionCookieAndSecureCookieDontMatchUp() {
        SessionId sessionId = SessionId.createNewSessionId();
        String secureCookie = "secure-cookie";
        when(hmacDigest.digest(sessionId.getSessionId())).thenReturn("session-id-hmac");
        when(hmacDigest.digest("session-id-hmac")).thenReturn("session-id-hmac-double");
        when(hmacDigest.digest(secureCookie)).thenReturn("secure-cookie-hmac");
        HmacValidator hmacValidator = new HmacValidator(hmacDigest);
        assertThat(hmacValidator.validateHMACSHA256(secureCookie, sessionId.getSessionId())).isEqualTo(false);
    }
}