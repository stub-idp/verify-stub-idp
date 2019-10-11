package stubsp.stubsp.cookies;

import stubidp.stubidp.cookies.CookieNames;

import javax.inject.Inject;

public class StubSpCookieNames implements CookieNames {
    public static final String SESSION_COOKIE_NAME = "x-stub-sp-session";
    public static final String SECURE_COOKIE_NAME = "x-stub-sp-secure-cookie";

    @Inject
    public StubSpCookieNames() {}

    @Override
    public String getSessionCookieName() {
        return SESSION_COOKIE_NAME;
    }

    @Override
    public String getSecureCookieName() {
        return SECURE_COOKIE_NAME;
    }
}
