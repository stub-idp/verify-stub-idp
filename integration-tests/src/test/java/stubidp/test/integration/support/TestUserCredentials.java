package stubidp.test.integration.support;

import org.mindrot.jbcrypt.BCrypt;
import stubidp.stubidp.configuration.UserCredentials;

public class TestUserCredentials extends UserCredentials {

    public TestUserCredentials(String user, String password) {
        this.user = user;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
