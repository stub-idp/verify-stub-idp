package uk.gov.ida.matchingserviceadapter.services;

import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.domain.matching.assertions.MatchingServiceAuthnStatement;
import stubidp.utils.security.security.IdGenerator;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class UnknownUserResponseGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(UnknownUserResponseGenerator.class);

    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;
    private final AssertionLifetimeConfiguration assertionLifetimeConfiguration;
    private final UserAccountCreationAttributeExtractor userAccountCreationAttributeExtractor;
    private final IdGenerator idGenerator;
    private final Clock clock;

    @Inject
    public UnknownUserResponseGenerator(
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            AssertionLifetimeConfiguration assertionLifetimeConfiguration,
            UserAccountCreationAttributeExtractor userAccountCreationAttributeExtractor,
            IdGenerator idGenerator) {
        this(matchingServiceAdapterConfiguration, assertionLifetimeConfiguration, userAccountCreationAttributeExtractor, idGenerator, Clock.systemUTC());
    }

    UnknownUserResponseGenerator(
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            AssertionLifetimeConfiguration assertionLifetimeConfiguration,
            UserAccountCreationAttributeExtractor userAccountCreationAttributeExtractor,
            IdGenerator idGenerator,
            Clock clock) {
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
        this.assertionLifetimeConfiguration = assertionLifetimeConfiguration;
        this.userAccountCreationAttributeExtractor = userAccountCreationAttributeExtractor;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    public OutboundResponseFromUnknownUserCreationService getMatchingServiceResponse(
        final UnknownUserCreationResponseDto unknownUserCreationResponseDto,
        final String requestId,
        final String hashPid,
        final String assertionConsumerServiceUrl,
        final String authnRequestIssuerId,
        final AssertionData assertionData,
        final List<Attribute> requestedUserAccountCreationAttributes) {
        if (unknownUserCreationResponseDto.getResult().equalsIgnoreCase(UnknownUserCreationResponseDto.FAILURE)) {
            return OutboundResponseFromUnknownUserCreationService.createFailure(idGenerator.getId(), requestId, matchingServiceAdapterConfiguration.getEntityId());
        }

        List<Attribute> userAttributesForAccountCreation =
                userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(
                        requestedUserAccountCreationAttributes,
                        assertionData.getMatchingDataset(),
                        assertionData.getCycle3Data()
                );

        if (userAttributesForAccountCreation.isEmpty()) {
            return OutboundResponseFromUnknownUserCreationService.createNoAttributeFailure(idGenerator.getId(), requestId, matchingServiceAdapterConfiguration.getEntityId());
        }

        AssertionRestrictions assertionRestrictions = new AssertionRestrictions(
            Instant.now(clock).plusMillis(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()),
            requestId,
            assertionConsumerServiceUrl);

        MatchingServiceAssertion assertion = new MatchingServiceAssertion(
                idGenerator.getId(),
                matchingServiceAdapterConfiguration.getEntityId(),
                Instant.now(clock),
                new PersistentId(hashPid),
                assertionRestrictions,
                MatchingServiceAuthnStatement.createIdaAuthnStatement(assertionData.getLevelOfAssurance()),
                authnRequestIssuerId,
                userAttributesForAccountCreation);

        return OutboundResponseFromUnknownUserCreationService.createSuccess(
            idGenerator.getId(),
            assertion,
            requestId,
            matchingServiceAdapterConfiguration.getEntityId()
        );
    }

}
