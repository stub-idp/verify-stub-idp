package uk.gov.ida.rp.testrp.builders;


import uk.gov.ida.common.shared.configuration.KeyConfiguration;

public class KeyConfigurationBuilder {

    private String keyUri = "private key";

    public static KeyConfigurationBuilder aKeyConfiguration() {
        return new KeyConfigurationBuilder();
    }

    public KeyConfiguration build() {
        return new TestKeyConfiguration(
                keyUri);
    }

    public KeyConfigurationBuilder withKeyUri(String uri) {
        this.keyUri = uri;
        return this;
    }

    private static final class TestKeyConfiguration extends KeyConfiguration {
        private TestKeyConfiguration(
                String keyUri) {

            this.keyUri = keyUri;
        }
    }
}
