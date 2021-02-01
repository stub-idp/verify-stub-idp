package uk.gov.ida.integrationTest.support;

import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import stubidp.test.devpki.TestCertificateStrings;
import uk.gov.ida.rp.testrp.TestRpApplication;
import uk.gov.ida.rp.testrp.TestRpConfiguration;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class TestRpAppRule extends DropwizardAppExtension<TestRpConfiguration> {

    private TestRpAppRule(final ConfigOverride[] configOverrides) {
        super(TestRpApplication.class, ResourceHelpers.resourceFilePath("test-rp.yml"), configOverrides);
    }

    public static TestRpAppRule newTestRpAppRule(final ConfigOverride... configOverrides) {

        ImmutableList<ConfigOverride> mergedConfigOverrides = ImmutableList.<ConfigOverride>builder()
                .add(ConfigOverride.config("privateSigningKeyConfiguration.key", TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY))
                .add(ConfigOverride.config("privateSigningKeyConfiguration.type", "encoded"))
                .add(ConfigOverride.config("publicSigningCert.cert", TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT))
                .add(ConfigOverride.config("publicSigningCert.type", "x509"))
                .add(ConfigOverride.config("privateEncryptionKeyConfiguration.key", TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY))
                .add(ConfigOverride.config("privateEncryptionKeyConfiguration.type", "encoded"))
                .add(ConfigOverride.config("publicEncryptionCert.cert", TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT))
                .add(ConfigOverride.config("publicEncryptionCert.type", "x509"))
                .add(configOverrides)
                .build();

        ConfigOverride[] configOverridesArray = mergedConfigOverrides.toArray(new ConfigOverride[mergedConfigOverrides.size()]);
        return new TestRpAppRule(configOverridesArray);
    }

    public UriBuilder uriBuilder (String path) {
        return UriBuilder.fromUri("http://localhost")
            .path(path)
            .port(getLocalPort());
    }

    public URI uri(String path) {
        return uriBuilder(path).build();
    }
}
