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
import uk.gov.ida.integrationTest.support.JourneyHelper;
import uk.gov.ida.integrationTest.support.RequestParamHelper;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.Urls;
import uk.gov.ida.rp.testrp.contract.MatchingServiceRequestDto;
import uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationRequestDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationResponseDto;

import javax.ws.rs.client.Client;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.rp.testrp.builders.Cycle3DatasetDtoBuilder.aCycle3DatasetDto;
import static uk.gov.ida.rp.testrp.builders.MatchingServiceRequestDtoBuilder.aMatchingServiceRequestDto;
import static uk.gov.ida.rp.testrp.builders.UniversalMatchingDatasetDtoBuilder.theSpecialCycle3UserMatchingDataset;
import static uk.gov.ida.rp.testrp.builders.UnknownUserCreationRequestDtoBuilder.anUnknownUserCreationRequestDto;
import static uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto.MATCH;
import static uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto.NO_MATCH;
import static uk.gov.ida.rp.testrp.contract.UnknownUserCreationResponseDto.FAILURE;

public class LocalMatchingServiceResourceAppRuleTests extends IntegrationTestHelper {
    private static Client client;
    private static JourneyHelper journeyHelper;

    @ClassRule
    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
        ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
        ConfigOverride.config("msaMetadataUri", "http://localhost:"+getMsaStubRule().getPort()+"/metadata"),
        ConfigOverride.config("allowInsecureMetadataLocation", "true"));

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(LocalMatchingServiceResourceAppRuleTests.class.getSimpleName());
        journeyHelper = new JourneyHelper(client);
    }

    @Test
    public void post_shouldReturnMatchResponseWhenRequestContainsCycle3Dataset() {
        final MatchingServiceRequestDto matchingServiceRequestDto = aMatchingServiceRequestDto()
            .withCycle3Dataset(aCycle3DatasetDto().build())
            .build();

        MatchingServiceResponseDto responseDto = journeyHelper.postMatchingRequest(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE),matchingServiceRequestDto);

        assertThat(responseDto.getResult()).isEqualTo(MATCH);
    }

    @Test
    public void post_shouldReturnNoMatchResponseWhenRequestDoesNotContainCycle3DatasetAndMatchingDatasetMatchesSpecialCycle3User() {
        final MatchingServiceRequestDto matchingServiceRequestDto = aMatchingServiceRequestDto()
            .withMatchingDataset(theSpecialCycle3UserMatchingDataset())
            .withCycle3Dataset(null)
            .build();

        MatchingServiceResponseDto responseDto = journeyHelper.postMatchingRequest(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), matchingServiceRequestDto);

        assertThat(responseDto.getResult()).isEqualTo(NO_MATCH);
    }

    // default should probably be failure though? - not changing it now
    @Test
    public void post_shouldReturnSuccessResponseWhenForUserAccountCreationByDefault() {
        final UnknownUserCreationRequestDto unknownUserCreationRequestDto = anUnknownUserCreationRequestDto().build();

        UnknownUserCreationResponseDto responseDto = journeyHelper.postUnknownUserCreation(testRp.uri(Urls.TestRpUrls.UNKNOWN_USER_CREATION_SERVICE_RESOURCE), unknownUserCreationRequestDto);

        assertThat(responseDto.getResult()).isEqualTo(UnknownUserCreationResponseDto.SUCCESS);
    }

    @Test
    public void post_shouldReturnFailureResponseWhenForUserAccountCreationWhenMapped() {
        RequestParamHelper.RequestParams requestParams = journeyHelper.startNewJourneyFromTestRp(testRp.uriBuilder(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE).build(), true, true);
        String hashedPid = "a-hashed-pid";

        final MatchingServiceRequestDto matchingServiceRequest = aMatchingServiceRequestDto()
            .withMatchId(requestParams.getRequestId().get())
            .withHashedPid(hashedPid)
            .build();

        MatchingServiceResponseDto matchingServiceResponseDto = journeyHelper.postMatchingRequest(testRp.uri(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_RESOURCE), matchingServiceRequest);

        assertThat(matchingServiceResponseDto.getResult()).isEqualTo(NO_MATCH);

        final UnknownUserCreationRequestDto unknownUserCreationRequestDto = anUnknownUserCreationRequestDto()
            .withHashedPid(hashedPid)
            .build();

        UnknownUserCreationResponseDto unknownUserCreationResponseDto = journeyHelper.postUnknownUserCreation(testRp.uri(Urls.TestRpUrls.UNKNOWN_USER_CREATION_SERVICE_RESOURCE), unknownUserCreationRequestDto);

        assertThat(unknownUserCreationResponseDto.getResult()).isEqualTo(FAILURE);
    }

}
