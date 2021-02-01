package uk.gov.ida.rp.testrp.builders;

import io.dropwizard.client.JerseyClientConfiguration;
import stubidp.saml.metadata.TrustStoreConfiguration;
import stubidp.utils.rest.common.ServiceInfoConfiguration;
import uk.gov.ida.rp.testrp.EntityId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.saml.configuration.SamlConfigurationImpl;

import static org.mockito.Mockito.mock;
import static stubidp.utils.rest.common.ServiceInfoConfigurationBuilder.aServiceInfo;
import static stubidp.utils.rest.jerseyclient.JerseyClientConfigurationBuilder.aJerseyClientConfiguration;

public class TestRpConfigurationBuilder {

    public class TestSamlConfigurationImpl extends SamlConfigurationImpl {
        public TestSamlConfigurationImpl(String issuer) {
            this.entityId = issuer;
        }
    }

    private SamlConfigurationImpl samlConfiguration = new TestSamlConfigurationImpl(EntityId.TEST_RP);
    private Boolean privateBetaUserAccessRestrictionEnabled = true;

    public static TestRpConfigurationBuilder aTestRpConfiguration() {
        return new TestRpConfigurationBuilder();
    }

    public TestRpConfiguration build() {

        return new TestTestRpConfiguration(
                aJerseyClientConfiguration().build(),
                aServiceInfo().withName("MatchingService").build(),
                mock(TrustStoreConfiguration.class),
                samlConfiguration,
                "cookie-name",
                false,
                false,
                "/javascript",
                "/stylesheets",
                "/images",
                privateBetaUserAccessRestrictionEnabled
        );
    }

    public TestRpConfigurationBuilder withAPrivateBetaUserAccessRestrictionEnabledValue(Boolean testRpUserAccessRestrictionEnabled) {
        this.privateBetaUserAccessRestrictionEnabled = testRpUserAccessRestrictionEnabled;
        return this;
    }

    private static class TestTestRpConfiguration extends TestRpConfiguration {

        private TestTestRpConfiguration(
                JerseyClientConfiguration httpClient,
                ServiceInfoConfiguration serviceInfo,
                TrustStoreConfiguration clientTrustStoreConfiguration,
                SamlConfigurationImpl saml,
                String cookieName,
                Boolean dontCacheFreemarkerTemplates,
                Boolean forceAuthentication,
                String javascriptPath,
                String stylesheetsPath,
                String imagesPath,
                Boolean privateBetaUserAccessRestrictionEnabled
        ) {

            this.httpClient = httpClient;
            this.serviceInfo = serviceInfo;
            this.clientTrustStoreConfiguration = clientTrustStoreConfiguration;

            this.saml = saml;
            this.cookieName = cookieName;
            this.dontCacheFreemarkerTemplates = dontCacheFreemarkerTemplates;
            this.forceAuthentication = forceAuthentication;
            this.javascriptPath = javascriptPath;
            this.stylesheetsPath = stylesheetsPath;
            this.imagesPath = imagesPath;
            this.privateBetaUserAccessRestrictionEnabled = privateBetaUserAccessRestrictionEnabled;
        }
    }
}
