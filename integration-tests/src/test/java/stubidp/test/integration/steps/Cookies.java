package stubidp.test.integration.steps;

import stubidp.shared.cookies.HttpOnlyNewCookie;
import stubidp.stubidp.cookies.StubIdpCookieNames;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class Cookies {
    private static final NewCookie[] NEW_COOKIES = new NewCookie[0];

    private final Map<String, NewCookie> cookies;

    public Cookies() {
        cookies = new HashMap<>();
    }

    public void extractCookies(Response response) {
        response.getCookies().forEach((k,v) -> {
            if (v.getMaxAge() == 0) {
                cookies.remove(k);
            } else {
                cookies.put(k, v);
            }
        });
    }

    public void setSessionCookie(String sessionId) {
        cookies.put(StubIdpCookieNames.SESSION_COOKIE_NAME,
                new HttpOnlyNewCookie(StubIdpCookieNames.SESSION_COOKIE_NAME,
                                        sessionId,
                                        "/",
                                        "",
                                        NewCookie.DEFAULT_MAX_AGE,
                                        false));
    }

    public NewCookie getSessionCookie() { return cookies.get(StubIdpCookieNames.SESSION_COOKIE_NAME); }

    public NewCookie getSecureCookie() { return cookies.get(StubIdpCookieNames.SECURE_COOKIE_NAME); }

    public NewCookie getCookie(String name) {
        return cookies.get(name);
    }

    public NewCookie[] getCookies() {
        return cookies.isEmpty() ? null : cookies.values().toArray(NEW_COOKIES);
    }

}
