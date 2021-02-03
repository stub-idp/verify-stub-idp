package uk.gov.ida.matchingserviceadapter.mappers;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.matching.MatchingServiceIdaStatus;
import stubidp.utils.security.security.IdGenerator;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static stubidp.saml.domain.matching.MatchingServiceIdaStatus.MatchingServiceMatch;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceResponseDtoBuilder.aMatchingServiceResponseDto;

@ExtendWith(MockitoExtension.class)
public class MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapperTest {

    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private AssertionLifetimeConfiguration assertionLifetimeConfiguration;

    private static final String ENTITY_ID = "entityId";
    private static final String TEST_ID = "testId";
    private static final String REQUEST_ID = "requestId";
    private static final String ASSERTION_CONSUMER_SERVICE_URL = "assertionConsumerServiceUrl";
    private static final String AUTHN_REQUEST_ISSUER_ID = "authnRequestIssuerId";
    private static final String HASH_PID = "hashPid";

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));

    private MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper mapper;

    @BeforeEach
    public void setup(){
        mapper = new MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper(configuration, assertionLifetimeConfiguration, idGenerator, clock);
    }

    @Test
    public void map_shouldTranslateMatchingServiceResponseDtoToIdaResponseFromMatchingServiceWithMatch() {
        when(assertionLifetimeConfiguration.getAssertionLifetime()).thenReturn(Duration.parse("30m"));
        when(configuration.getEntityId()).thenReturn(ENTITY_ID);
        when(idGenerator.getId()).thenReturn(TEST_ID);

        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withMatch().build();
        OutboundResponseFromMatchingService responseFromMatchingService = mapper.map(
            response,
            HASH_PID,
            REQUEST_ID,
            ASSERTION_CONSUMER_SERVICE_URL,
            AuthnContext.LEVEL_2,
            AUTHN_REQUEST_ISSUER_ID);

        assertThat(responseFromMatchingService.getStatus()).isEqualTo(MatchingServiceMatch);
        assertThat(responseFromMatchingService.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(responseFromMatchingService.getId()).isEqualTo(TEST_ID);
        assertThat(responseFromMatchingService.getIssuer()).isEqualTo(ENTITY_ID);
        assertThat(responseFromMatchingService.getMatchingServiceAssertion()).isPresent();
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getAssertionRestrictions()).isNotNull();
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getId()).isEqualTo(TEST_ID);
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getIssuerId()).isEqualTo(ENTITY_ID);
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getAssertionRestrictions().getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getAssertionRestrictions().getNotOnOrAfter()).isEqualTo(Instant.now(clock).plusMillis(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()));
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getAssertionRestrictions().getRecipient()).isEqualTo(ASSERTION_CONSUMER_SERVICE_URL);
    }

    @Test
    public void map_shouldTranslateMatchingServiceResponseDtoToIdaResponseFromMatchingServiceWithNoMatch() {
        when(configuration.getEntityId()).thenReturn(ENTITY_ID);
        when(idGenerator.getId()).thenReturn(TEST_ID);

        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withNoMatch().build();

        OutboundResponseFromMatchingService idaResponse = mapper.map(
            response,
            HASH_PID,
            REQUEST_ID,
            ASSERTION_CONSUMER_SERVICE_URL,
            AuthnContext.LEVEL_2,
            AUTHN_REQUEST_ISSUER_ID);

        assertThat(idaResponse.getStatus()).isEqualTo(MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService);
    }

    @Test
    public void map_shouldThrowExceptionIfNotNoMatchOrMatch() {
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withBadResponse().build();

        assertThrows(UnsupportedOperationException.class, () -> mapper.map(
            response,
            HASH_PID,
            REQUEST_ID,
            ASSERTION_CONSUMER_SERVICE_URL,
            AuthnContext.LEVEL_2,
            AUTHN_REQUEST_ISSUER_ID));
    }
}
