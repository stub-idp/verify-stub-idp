package uk.gov.ida.integrationTest;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.integrationTest.support.IntegrationTestHelper;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.rp.testrp.filters.SecurityHeadersFilterTest;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class SecurityHeadersIntegrationTests extends IntegrationTestHelper {

    private final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    public static final TestRpAppRule applicationRule = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "http://localhost:"+getMsaStubRule().getPort()+"/metadata"),
            ConfigOverride.config("allowInsecureMetadataLocation", "true"));

    @Test
    public void securityHeaderTest() {
        final Response response = client.target(UriBuilder.fromUri("http://localhost:" + applicationRule.getLocalPort())
                .path("/page_does_not_exist")
                .build())
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
        SecurityHeadersFilterTest.checkSecurityHeaders(response.getHeaders());
    }

}
