package uk.gov.ida.integrationTest;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.util.Duration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.integrationTest.support.IntegrationTestHelper;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.Urls;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestRpEidasJourneyDisabledAppRuleTest extends IntegrationTestHelper {
    private static Client client;

    @ClassRule
    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
        ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "http://localhost:"+getMsaStubRule().getPort()+"/metadata"),
        ConfigOverride.config("shouldShowStartWithEidasButton", "false"),
        ConfigOverride.config("allowInsecureMetadataLocation", "true"));

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(TestRpResourceAppRuleTests.class.getSimpleName());
    }

    @Test
    public void getLandingPage_shouldNotHaveEidasStartButton() throws Exception {
        URI uri = testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT);
        Response response = client.target(uri)
            .request(MediaType.TEXT_HTML)
            .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String html = response.readEntity(String.class);
        assertTrue(html.contains("Test GOV.UK Verify user journeys"));
        assertFalse(html.contains("Start with your European eID</button>"));
    }
}
