package stubidp.stubidp.cookies;

import stubidp.shared.cookies.CookieNames;

import javax.inject.Inject;

public class StubIdpCookieNames implements CookieNames {
    public static final String SESSION_COOKIE_NAME = "x-stub-idp-session";
    public static final String SECURE_COOKIE_NAME = "x-stub-idp-secure-cookie";

    @Inject
    public StubIdpCookieNames() {}

    @Override
    public String getSessionCookieName() {
        return SESSION_COOKIE_NAME;
    }

    @Override
    public String getSecureCookieName() {
        return SECURE_COOKIE_NAME;
    }
}
