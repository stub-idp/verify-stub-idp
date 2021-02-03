package uk.gov.ida.matchingserviceadapter.mappers;

import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.domain.matching.assertions.MatchingServiceAuthnStatement;
import stubidp.utils.security.security.IdGenerator;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;

import static java.util.Collections.emptyList;

public class MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper {
    private final MatchingServiceAdapterConfiguration configuration;
    private final AssertionLifetimeConfiguration assertionLifetimeConfiguration;
    private final IdGenerator idGenerator;
    private final Clock clock;

    @Inject
    public MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper(
            MatchingServiceAdapterConfiguration configuration,
            AssertionLifetimeConfiguration assertionLifetimeConfiguration,
            IdGenerator idGenerator) {
        this(configuration, assertionLifetimeConfiguration, idGenerator, Clock.systemUTC());
    }

    MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper(
            MatchingServiceAdapterConfiguration configuration,
            AssertionLifetimeConfiguration assertionLifetimeConfiguration,
            IdGenerator idGenerator,
            Clock clock) {
        this.configuration = configuration;
        this.assertionLifetimeConfiguration = assertionLifetimeConfiguration;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    public OutboundResponseFromMatchingService map(
        MatchingServiceResponseDto response,
        String hashPid,
        String requestId,
        String assertionConsumerServiceUrl,
        AuthnContext authnContext,
        String authnRequestIssuerId) {

        String result = response.getResult();

        switch (result) {
            case MatchingServiceResponseDto.MATCH:
                return getMatchResponse(hashPid, requestId, assertionConsumerServiceUrl, authnContext, authnRequestIssuerId);
            case MatchingServiceResponseDto.NO_MATCH:
                return OutboundResponseFromMatchingService.createNoMatchFromMatchingService(idGenerator.getId(), requestId, configuration.getEntityId());
            default:
                throw new UnsupportedOperationException("The matching service has returned an unsupported response message.");
        }
    }

    private OutboundResponseFromMatchingService getMatchResponse(
        final String hashPid,
        final String requestId,
        final String assertionConsumerServiceUrl,
        final AuthnContext authnContext,
        final String authnRequestIssuerId) {

        AssertionRestrictions assertionRestrictions = new AssertionRestrictions(
                Instant.now(clock).plusMillis(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()),
                requestId,
                assertionConsumerServiceUrl);

        MatchingServiceAssertion assertion = new MatchingServiceAssertion(
                idGenerator.getId(),
                configuration.getEntityId(),
                Instant.now(clock),
                new PersistentId(hashPid),
                assertionRestrictions,
                MatchingServiceAuthnStatement.createIdaAuthnStatement(authnContext),
                authnRequestIssuerId,
                emptyList());

        return OutboundResponseFromMatchingService.createMatchFromMatchingService(
                idGenerator.getId(),
                assertion,
                requestId,
                configuration.getEntityId()
        );
    }
}
