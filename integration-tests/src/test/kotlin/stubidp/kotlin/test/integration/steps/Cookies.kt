package stubidp.kotlin.test.integration.steps

import stubidp.stubidp.cookies.StubIdpCookieNames
import java.util.HashMap
import javax.ws.rs.core.NewCookie
import javax.ws.rs.core.Response

class Cookies {
    var cookies: MutableMap<String, NewCookie> = HashMap()
    fun extractCookies(response: Response?) {
        response!!.cookies.forEach { (k: String, v: NewCookie) ->
            if (v.maxAge == 0) {
                cookies.remove(k)
            } else {
                cookies[k] = v
            }
        }
    }

    val sessionCookie: NewCookie?
        get() = cookies[StubIdpCookieNames.SESSION_COOKIE_NAME]

    val secureCookie: NewCookie?
        get() = cookies[StubIdpCookieNames.SECURE_COOKIE_NAME]

}