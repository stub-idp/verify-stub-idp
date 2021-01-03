package stubidp.stubidp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import stubidp.saml.domain.assertions.IdpIdaStatus;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.shared.repositories.MetadataRepository;
import stubidp.stubidp.domain.FraudIndicator;
import stubidp.saml.domain.response.OutboundResponseFromIdp;
import stubidp.stubidp.domain.factories.AssertionFactory;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.saml.transformers.outbound.OutboundResponseFromIdpTransformerProvider;
import stubidp.utils.rest.common.SessionId;
import stubidp.utils.security.security.IdGenerator;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.saml.domain.assertions.AuthnContext.LEVEL_2;

@ExtendWith(MockitoExtension.class)
public class NonSuccessAuthnResponseServiceTest {

    private final String IDP_NAME = "an idp name";
    private final String REQUEST_ID = "requestId";
    private final URI HUB_URI = URI.create("hub");
    private final String IDP_ISSUER = "idpissuer";
    private final String RELAY_STATE = "relayrelayrelay";

    private NonSuccessAuthnResponseService nonSuccessAuthnResponseService;

    @Mock
    private IdpStubsRepository idpStubsRepository;
    @Mock
    private MetadataRepository metadataRepository;
    @Mock
    private AssertionFactory assertionFactory;
    @Mock
    private OutboundResponseFromIdpTransformerProvider outboundResponseFromIdpTransformerProvider;
    @Mock
    private Idp idp;
    @Mock
    private Function<OutboundResponseFromIdp, String> transformer;

    @Captor
    private ArgumentCaptor<OutboundResponseFromIdp> captor;

    @BeforeEach
    void createResource() {
        nonSuccessAuthnResponseService = new NonSuccessAuthnResponseService(
                new IdGenerator(),
                idpStubsRepository,
                metadataRepository,
                assertionFactory,
                outboundResponseFromIdpTransformerProvider);
        when(idpStubsRepository.getIdpWithFriendlyId(IDP_NAME)).thenReturn(idp);
        when(idp.getIssuerId()).thenReturn(IDP_ISSUER);
        when(metadataRepository.getAssertionConsumerServiceLocation()).thenReturn(HUB_URI);
        when(outboundResponseFromIdpTransformerProvider.get(idp)).thenReturn(transformer);
    }

    @Test
    void shouldBuildAuthnPending(){
        nonSuccessAuthnResponseService.generateAuthnPending(IDP_NAME, REQUEST_ID, RELAY_STATE).getResponseString();

        verify(transformer).apply(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(IdpIdaStatus.authenticationPending());
    }

    @Test
    void shouldBuildNoAuthnContext(){
        nonSuccessAuthnResponseService.generateNoAuthnContext(IDP_NAME, REQUEST_ID, RELAY_STATE).getResponseString();

        verify(transformer).apply(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(IdpIdaStatus.noAuthenticationContext());
    }

    @Test
    void shouldBuildUpliftFailed(){
        nonSuccessAuthnResponseService.generateUpliftFailed(IDP_NAME, REQUEST_ID, RELAY_STATE).getResponseString();

        verify(transformer).apply(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(IdpIdaStatus.upliftFailed());
    }

    @Test
    void shouldBuildNoAuthnCancel(){
        nonSuccessAuthnResponseService.generateAuthnCancel(IDP_NAME, REQUEST_ID, RELAY_STATE).getResponseString();

        verify(transformer).apply(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(IdpIdaStatus.authenticationCancelled());
    }

    @Test
    void shouldBuildNoAuthnFailed(){
        nonSuccessAuthnResponseService.generateAuthnFailed(IDP_NAME, REQUEST_ID, RELAY_STATE).getResponseString();

        verify(transformer).apply(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(IdpIdaStatus.authenticationFailed());
    }

    @Test
    void shouldBuildRequesterError(){
        nonSuccessAuthnResponseService.generateRequesterError(REQUEST_ID, "error", IDP_NAME, RELAY_STATE).getResponseString();

        verify(transformer).apply(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(IdpIdaStatus.requesterError(Optional.of("error")));
    }

    @Test
    void shouldBuildFraudResponse(){
        IdpSession session = new IdpSession(SessionId.createNewSessionId(),
                IdaAuthnRequestFromHub.createRequestReceivedFromHub(REQUEST_ID, HUB_URI.toString(), List.of(LEVEL_2), false, Instant.now(), AuthnContextComparisonTypeEnumeration.EXACT),
                RELAY_STATE, null, null, null, null, null, null);
        nonSuccessAuthnResponseService.generateFraudResponse(IDP_NAME, REQUEST_ID, FraudIndicator.FI01, "ipAddress", session).getResponseString();

        verify(transformer).apply(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(IdpIdaStatus.success());
    }
}
