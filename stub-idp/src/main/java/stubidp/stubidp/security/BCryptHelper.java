package stubidp.stubidp.security;

import io.dropwizard.util.Strings;

import java.util.Objects;

public class BCryptHelper {

    private BCryptHelper() {}

    public static boolean alreadyCrypted(String password) {
        return Objects.nonNull(password) &&
                password.length() == 60 &&
                password.charAt(0) == '$' &&
                password.charAt(3) == '$' &&
                password.charAt(6) == '$' &&
                password.split("\\$").length == 4;
    }
}
