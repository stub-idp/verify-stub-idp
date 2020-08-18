package stubidp.kotlin.test.integration.support

import org.mindrot.jbcrypt.BCrypt
import stubidp.stubidp.configuration.UserCredentials

class TestUserCredentials(user: String?, password: String?) : UserCredentials() {
    init {
        this.user = user
        this.password = BCrypt.hashpw(password, BCrypt.gensalt())
    }
}