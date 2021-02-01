package uk.gov.ida.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jwt.SignedJWT;
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
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.tokenservice.GenerateTokenRequestDto;
import uk.gov.ida.rp.testrp.tokenservice.TokenDto;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.devpki.TestEntityIds.STUB_IDP_ONE;

@ExtendWith(DropwizardExtensionsSupport.class)
public class TokenResourceAppRuleTests extends IntegrationTestHelper {
    private static Client client;

    private static final int TOKEN_EPOCH = 5;

    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "http://localhost:"+getMsaStubRule().getPort()+"/metadata"),
            ConfigOverride.config("allowInsecureMetadataLocation", "true"),
            ConfigOverride.config("tokenEpoch", String.valueOf(TOKEN_EPOCH)));

    @BeforeAll
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(TokenResourceAppRuleTests.class.getSimpleName());
    }

    @Test
    public void shouldGenerateToken() throws Exception {
        URI uri = testRp.uri(Urls.PrivateUrls.GENERATE_TOKEN_RESOURCE);
        Instant validUntil = Instant.now().plus(5, ChronoUnit.DAYS);
        Response response = client
                .target(uri)
                .request()
                .post(Entity.json(new GenerateTokenRequestDto(validUntil, STUB_IDP_ONE)));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        AccessToken token = response.readEntity(AccessToken.class);
        TokenDto tokenDto = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(SignedJWT.parse(token.getTokenValue()).getPayload().toString(), TokenDto.class);
        assertThat(tokenDto.getValidUntil()).isEqualTo(validUntil);
        assertThat(tokenDto.getIssuedTo()).isEqualTo(STUB_IDP_ONE);
        assertThat(tokenDto.getEpoch()).isEqualTo(TOKEN_EPOCH);
    }
}
