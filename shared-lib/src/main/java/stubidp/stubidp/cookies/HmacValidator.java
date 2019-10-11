package stubidp.stubidp.cookies;

import stubidp.utils.security.security.HmacDigest;

import javax.inject.Inject;

public class HmacValidator {
    private HmacDigest hmacDigest;

    @Inject
    public HmacValidator(HmacDigest hmacDigest) {
        this.hmacDigest = hmacDigest;
    }

    // Do a double HMAC verification to avoid a potential timing related side-channel
    // See https://www.isecpartners.com/blog/2011/february/double-hmac-verification.aspx
    public boolean validateHMACSHA256(final String cookieValue, final String sessionId) {
        final String sessionIdHmac = hmacDigest.digest(sessionId);
        final String sessionIdHmacDouble = hmacDigest.digest(sessionIdHmac);

        final String cookieValueHmacDouble = hmacDigest.digest(cookieValue);

        return cookieValueHmacDouble.equals(sessionIdHmacDouble);
    }
}
