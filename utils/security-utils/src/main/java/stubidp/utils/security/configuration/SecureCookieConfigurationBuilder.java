package stubidp.utils.security.configuration;


public class SecureCookieConfigurationBuilder {

    private SecureCookieConfigurationBuilder() {
    }

    public static SecureCookieConfigurationBuilder aSecureCookieConfiguration() {
        return new SecureCookieConfigurationBuilder();
    }

    public SecureCookieConfiguration build() {
        return new TestSecureCookieConfiguration(
                KeyConfigurationBuilder.aKeyConfiguration().build(),
                false);
    }

    private static class TestSecureCookieConfiguration extends SecureCookieConfiguration {
        private TestSecureCookieConfiguration(
                KeyConfiguration keyConfiguration,
                boolean secure) {
            this.keyConfiguration = keyConfiguration;
            this.secure = secure;
        }
    }
}
