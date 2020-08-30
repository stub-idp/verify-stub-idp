package stubidp.stubidp.services;

import io.prometheus.client.Counter;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.assertions.IpAddress;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.shared.repositories.MetadataRepository;
import stubidp.stubidp.StubIdpIdpBinder;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.saml.domain.response.OutboundResponseFromIdp;
import stubidp.stubidp.domain.SamlResponseFromValue;
import stubidp.stubidp.domain.factories.AssertionRestrictionsFactory;
import stubidp.stubidp.domain.factories.IdentityProviderAssertionFactory;
import stubidp.stubidp.domain.factories.MatchingDatasetFactory;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.resources.idp.HeadlessIdpResource;
import stubidp.stubidp.saml.transformers.outbound.OutboundResponseFromIdpTransformerProvider;
import stubidp.utils.security.security.IdGenerator;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.util.UUID;

import static stubidp.saml.domain.assertions.IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement;

public class SuccessAuthnResponseService {

    private static final String STUBIDP_VERIFY_SENT_AUTHN_RESPONSES_SUCCESS_TOTAL = "stubidp_verify_sentAuthnResponses_success_total";
    public static final Counter sentVerifyAuthnResponses = Counter.build()
            .name(STUBIDP_VERIFY_SENT_AUTHN_RESPONSES_SUCCESS_TOTAL)
            .help("Number of sent verify authn responses.")
            .register();

    private final IdGenerator idGenerator;
    private final IdentityProviderAssertionFactory identityProviderAssertionFactory;
    private final IdpStubsRepository idpStubsRepository;
    private final MetadataRepository metadataProvider;
    private final AssertionRestrictionsFactory assertionRestrictionsFactory;
    private final OutboundResponseFromIdpTransformerProvider outboundResponseFromIdpTransformerProvider;

    @Inject
    public SuccessAuthnResponseService(
            IdGenerator idGenerator,
            IdentityProviderAssertionFactory identityProviderAssertionFactory,
            IdpStubsRepository idpStubsRepository,
            @Named(StubIdpIdpBinder.HUB_METADATA_REPOSITORY) MetadataRepository metadataProvider,
            AssertionRestrictionsFactory assertionRestrictionsFactory,
            OutboundResponseFromIdpTransformerProvider outboundResponseFromIdpTransformerProvider) {
        this.idGenerator = idGenerator;

        this.identityProviderAssertionFactory = identityProviderAssertionFactory;
        this.idpStubsRepository = idpStubsRepository;
        this.metadataProvider = metadataProvider;
        this.assertionRestrictionsFactory = assertionRestrictionsFactory;
        this.outboundResponseFromIdpTransformerProvider = outboundResponseFromIdpTransformerProvider;
    }

    public SamlResponseFromValue<OutboundResponseFromIdp> getSuccessResponse(boolean randomisePid, String remoteIpAddress, String idpName, IdpSession session) {
        URI hubUrl = metadataProvider.getAssertionConsumerServiceLocation();
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);

        DatabaseIdpUser idpUser = session.getIdpUser().get();
        String requestId = session.getIdaAuthnRequestFromHub().getId();

        PersistentId persistentId = new PersistentId(idpUser.getPersistentId());
        if(randomisePid) {
            persistentId = new PersistentId(UUID.randomUUID().toString());
        }

        IdentityProviderAssertion matchingDatasetAssertion = identityProviderAssertionFactory.createMatchingDatasetAssertion(
                persistentId,
                idp.getIssuerId(),
                MatchingDatasetFactory.create(idpUser),
                assertionRestrictionsFactory.createRestrictionsForSendingToHub(requestId));

        IdentityProviderAssertion authnStatementAssertion = identityProviderAssertionFactory.createAuthnStatementAssertion(
                persistentId,
                idp.getIssuerId(),
                createIdentityProviderAuthnStatement(idpUser.getLevelOfAssurance(), new IpAddress(remoteIpAddress)),
                assertionRestrictionsFactory.createRestrictionsForSendingToHub(requestId));

        OutboundResponseFromIdp idaResponse = OutboundResponseFromIdp.createSuccessResponseFromIdp(
                idGenerator.getId(),
                session.getIdaAuthnRequestFromHub().getId(),
                idp.getIssuerId(),
                matchingDatasetAssertion,
                authnStatementAssertion,
                hubUrl);

        if(!HeadlessIdpResource.IDP_NAME.equals(idpName)) {
            sentVerifyAuthnResponses.inc();
        }

        return new SamlResponseFromValue<>(idaResponse, outboundResponseFromIdpTransformerProvider.get(idp), session.getRelayState(), hubUrl);
    }

}
