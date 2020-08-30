package stubidp.stubidp.services;

import io.prometheus.client.Counter;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.assertions.IpAddress;
import stubidp.shared.domain.SamlResponse;
import stubidp.shared.repositories.MetadataRepository;
import stubidp.stubidp.StubIdpIdpBinder;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.FraudIndicator;
import stubidp.saml.domain.response.OutboundResponseFromIdp;
import stubidp.stubidp.domain.SamlResponseFromValue;
import stubidp.stubidp.domain.factories.AssertionFactory;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.saml.transformers.outbound.OutboundResponseFromIdpTransformerProvider;
import stubidp.utils.security.security.IdGenerator;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

public class NonSuccessAuthnResponseService {

    private static final String STUBIDP_VERIFY_SENT_AUTHN_RESPONSES_FAILURE_TOTAL = "stubidp_verify_sentAuthnResponses_failure_total";

    public static final Counter sentVerifyAuthnFailureResponses = Counter.build()
            .name(STUBIDP_VERIFY_SENT_AUTHN_RESPONSES_FAILURE_TOTAL)
            .help("Number of sent verify authn failure responses.")
            .labelNames("failure_type")
            .register();

    private enum FailureType {
        fraud,
        authn_pending,
        uplift_failed,
        authn_cancel,
        no_authn_context,
        authn_failed,
        requester_error
    }

    private final IdGenerator idGenerator;
    private final IdpStubsRepository idpStubsRepository;
    private final MetadataRepository metadataRepository;
    private final AssertionFactory assertionFactory;
    private final OutboundResponseFromIdpTransformerProvider outboundResponseFromIdpTransformerProvider;

    @Inject
    public NonSuccessAuthnResponseService(
            IdGenerator idGenerator,
            IdpStubsRepository idpStubsRepository,
            @Named(StubIdpIdpBinder.HUB_METADATA_REPOSITORY) MetadataRepository metadataRepository,
            AssertionFactory assertionFactory,
            OutboundResponseFromIdpTransformerProvider outboundResponseFromIdpTransformerProvider) {
        this.idGenerator = idGenerator;

        this.idpStubsRepository = idpStubsRepository;
        this.metadataRepository = metadataRepository;
        this.assertionFactory = assertionFactory;
        this.outboundResponseFromIdpTransformerProvider = outboundResponseFromIdpTransformerProvider;
    }

    public SamlResponse generateFraudResponse(String idpName, String samlRequestId, FraudIndicator fraudIndicatorParam, String clientIpAddress, IdpSession session) {
        String requestId = session.getIdaAuthnRequestFromHub().getId();
        DatabaseIdpUser idpUser = IdpUserService.createRandomUser();
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        URI hubUrl = metadataRepository.getAssertionConsumerServiceLocation();

        IdentityProviderAssertion matchingDatasetAssertion = assertionFactory.createMatchingDatasetAssertion(idp.getIssuerId(), idpUser, requestId);
        IdentityProviderAssertion authnStatementAssertion = assertionFactory.createFraudAuthnStatementAssertion(
                idp.getIssuerId(),
                idpUser,
                requestId,
                idpName,
                fraudIndicatorParam.name(),
                new IpAddress(clientIpAddress));

        OutboundResponseFromIdp successResponseFromIdp = OutboundResponseFromIdp.createSuccessResponseFromIdp(
                idGenerator.getId(),
                samlRequestId,
                idp.getIssuerId(),
                matchingDatasetAssertion,
                authnStatementAssertion,
                hubUrl);

        return generateResponse(idp, successResponseFromIdp, hubUrl, session.getRelayState(), FailureType.fraud);
    }

    public SamlResponse generateAuthnPending(String idpName, String samlRequestId, String relayState) {
        URI hubUrl = metadataRepository.getAssertionConsumerServiceLocation();
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        OutboundResponseFromIdp authnPendingResponseIssuedByIdp = OutboundResponseFromIdp.createAuthnPendingResponseIssuedByIdp(idGenerator.getId(), samlRequestId, idp.getIssuerId(), hubUrl);
        return generateResponse(idp, authnPendingResponseIssuedByIdp, hubUrl, relayState, FailureType.authn_pending);
    }

    public SamlResponse generateUpliftFailed(String idpName, String samlRequestId, String relayState) {
        URI hubUrl = metadataRepository.getAssertionConsumerServiceLocation();
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        OutboundResponseFromIdp upliftFailedResponseIssuedByIdp = OutboundResponseFromIdp.createUpliftFailedResponseIssuedByIdp(idGenerator.getId(), samlRequestId, idp.getIssuerId(), hubUrl);
        return generateResponse(idp, upliftFailedResponseIssuedByIdp, hubUrl, relayState, FailureType.uplift_failed);
    }

    public SamlResponse generateAuthnCancel(String idpName, String samlRequestId, String relayState) {
        URI hubUrl = metadataRepository.getAssertionConsumerServiceLocation();
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        OutboundResponseFromIdp authnCancelResponseIssuedByIdp = OutboundResponseFromIdp.createAuthnCancelResponseIssuedByIdp(idGenerator.getId(), samlRequestId, idp.getIssuerId(), hubUrl);
        return generateResponse(idp, authnCancelResponseIssuedByIdp, hubUrl, relayState, FailureType.authn_cancel);
    }

    public SamlResponse generateNoAuthnContext(String idpName, String samlRequestId, String relayState) {
        URI hubUrl = metadataRepository.getAssertionConsumerServiceLocation();
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        OutboundResponseFromIdp noAuthnContextResponseIssuedByIdp = OutboundResponseFromIdp.createNoAuthnContextResponseIssuedByIdp(idGenerator.getId(), samlRequestId, idp.getIssuerId(), hubUrl);
        return generateResponse(idp, noAuthnContextResponseIssuedByIdp, hubUrl, relayState, FailureType.no_authn_context);
    }

    public SamlResponse generateAuthnFailed(String idpName, String samlRequestId, String relayState) {
        URI hubUrl = metadataRepository.getAssertionConsumerServiceLocation();
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        OutboundResponseFromIdp failureResponse = OutboundResponseFromIdp.createAuthnFailedResponseIssuedByIdp(idGenerator.getId(), samlRequestId, idp.getIssuerId(), hubUrl);
        return generateResponse(idp, failureResponse, hubUrl, relayState, FailureType.authn_failed);
    }

    public SamlResponse generateRequesterError(String samlRequestId, String requesterErrorMessage, String idpName, String relayState) {
        URI hubUrl = metadataRepository.getAssertionConsumerServiceLocation();
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        OutboundResponseFromIdp requesterErrorResponse = OutboundResponseFromIdp.createRequesterErrorResponseIssuedByIdp(
                idGenerator.getId(),
                samlRequestId,
                idp.getIssuerId(),
                hubUrl,
                requesterErrorMessage);
        return generateResponse(idp, requesterErrorResponse, hubUrl, relayState, FailureType.requester_error);
    }

    private SamlResponseFromValue<OutboundResponseFromIdp> generateResponse(Idp idp, OutboundResponseFromIdp outboundResponseFromIdp, URI hubUrl, String relayState, FailureType failureType) {
        sentVerifyAuthnFailureResponses.labels(failureType.name()).inc();
        return new SamlResponseFromValue<>(outboundResponseFromIdp, outboundResponseFromIdpTransformerProvider.get(idp), relayState, hubUrl);
    }

}
