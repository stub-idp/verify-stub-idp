package uk.gov.ida.integrationTest;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.utils.rest.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.integrationTest.support.IntegrationTestHelper;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.rp.testrp.Urls;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class TestRpEidasJourneyAppRuleTest extends IntegrationTestHelper {
    private static Client client;

    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
        ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "http://localhost:"+getMsaStubRule().getPort()+"/metadata"),
        ConfigOverride.config("allowInsecureMetadataLocation", "true"));

    @BeforeAll
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(TestRpResourceAppRuleTests.class.getSimpleName());
    }

    @Test
    public void getTestRpSamlRedirectView_shouldHaveEidasJourneyHint() {
        URI uri = testRp.uriBuilder(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE)
            .queryParam("eidas", "true")
            .build();

        Response response = client.target(uri)
            .request(MediaType.TEXT_HTML)
            .get(Response.class);

        String html = response.readEntity(String.class);
        assertThat(html).contains("value=\"eidas_sign_in\"");
    }

    @Test
    public void getHeadlessRpIdpStartSamlRedirectView_shouldHaveIdpStartJourneyHint() {
        URI uri = testRp.uriBuilder(Urls.HeadlessUrls.SUCCESS_PATH_IDP)
            .build();

        Response response = client.target(uri)
            .request(MediaType.TEXT_HTML)
            .get(Response.class);

        String html = response.readEntity(String.class);

        assertThat(html).contains("value=\"uk_idp_start\"");
    }

    @Test
    public void getHeadlessRpStartSamlRedirectView_shouldHaveNoJourneyHint() {
        URI uri = testRp.uriBuilder(Urls.HeadlessUrls.SUCCESS_PATH)
                .build();

        Response response = client.target(uri)
                .request(MediaType.TEXT_HTML)
                .get(Response.class);

        String html = response.readEntity(String.class);

        assertThat(html).doesNotContain("value=\"submission_confirmation\"");
        assertThat(html).doesNotContain("value=\"registration\"");
        assertThat(html).doesNotContain("value=\"uk_idp_sign_in\"");
        assertThat(html).doesNotContain("value=\"uk_idp_start\"");
        assertThat(html).doesNotContain("value=\"eidas_sign_in\"");
        assertThat(html).doesNotContain("value=\"unspecified\"");
    }
}
