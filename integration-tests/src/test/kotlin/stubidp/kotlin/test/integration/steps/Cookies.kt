package stubidp.kotlin.test.integration.steps

import stubidp.shared.cookies.HttpOnlyNewCookie
import stubidp.stubidp.cookies.StubIdpCookieNames
import java.util.HashMap
import javax.ws.rs.core.NewCookie
import javax.ws.rs.core.Response

class Cookies {
    var cookies: MutableMap<String, NewCookie>
    fun extractCookies(response: Response?) {
        response!!.cookies.forEach { (k: String, v: NewCookie) ->
            if (v.maxAge == 0) {
                cookies.remove(k)
            } else {
                cookies[k] = v
            }
        }
    }

    fun setSessionCookie(sessionId: String?) {
        cookies[StubIdpCookieNames.SESSION_COOKIE_NAME] = HttpOnlyNewCookie(StubIdpCookieNames.SESSION_COOKIE_NAME,
                sessionId,
                "/",
                "",
                NewCookie.DEFAULT_MAX_AGE,
                false)
    }

    val sessionCookie: NewCookie?
        get() = cookies[StubIdpCookieNames.SESSION_COOKIE_NAME]

    val secureCookie: NewCookie?
        get() = cookies[StubIdpCookieNames.SECURE_COOKIE_NAME]

    fun getCookie(name: String): NewCookie? {
        return cookies[name]
    }

    fun getCookies(): Array<NewCookie>? {
        return if (cookies.isEmpty()) null else cookies.values.toTypedArray()
    }

    init {
        cookies = HashMap()
    }
}