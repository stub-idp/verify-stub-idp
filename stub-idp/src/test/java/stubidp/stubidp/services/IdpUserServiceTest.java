package stubidp.stubidp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.factories.MatchingDatasetFactoryTest;
import stubidp.stubidp.exceptions.IncompleteRegistrationException;
import stubidp.stubidp.exceptions.InvalidDateException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.exceptions.UsernameAlreadyTakenException;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.utils.rest.common.SessionId;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdpUserServiceTest {

    private final String RELAY_STATE = "relayState";
    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String IDP_NAME = "an idp name";
    private final SessionId SESSION_ID = SessionId.createNewSessionId();

    private IdpUserService idpUserService;

    @Mock
    private IdpSessionRepository sessionRepository;
    @Mock
    private Idp idp;
    @Mock
    private IdaAuthnRequestFromHub idaAuthnRequestFromHubOptional;
    @Mock
    private IdpStubsRepository idpStubsRepository;

    @BeforeEach
    void createResource() {
        idpUserService = new IdpUserService(sessionRepository, idpStubsRepository);
    }

    @Test
    void shouldBuildSuccessResponse() throws InvalidUsernameOrPasswordException, InvalidSessionIdException {
        when(idpStubsRepository.getIdpWithFriendlyId(IDP_NAME)).thenReturn(idp);
        Optional<DatabaseIdpUser> idpUserOptional = Optional.of(MatchingDatasetFactoryTest.completeUser);
        when(idp.getUser(USERNAME, PASSWORD)).thenReturn(idpUserOptional);
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.of(new IdpSession(SESSION_ID, Instant.now(), idaAuthnRequestFromHubOptional, RELAY_STATE, null, null, null, null, null, null)));

        idpUserService.attachIdpUserToSession(IDP_NAME, USERNAME, PASSWORD, SESSION_ID);

        ArgumentCaptor<IdpSession> argumentCaptor = ArgumentCaptor.forClass(IdpSession.class);
        verify(sessionRepository, times(1)).updateSession(eq(SESSION_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getIdpUser()).isEqualTo(idpUserOptional);
    }

    @Test
    void shouldHaveStatusSuccessResponseWhenUserRegisters() throws InvalidSessionIdException, IncompleteRegistrationException, InvalidDateException, UsernameAlreadyTakenException, InvalidUsernameOrPasswordException {
        IdpSession session = new IdpSession(SessionId.createNewSessionId(), Instant.now(), idaAuthnRequestFromHubOptional, "test-relay-state", Collections.emptyList(), Collections.emptyList(), Optional.empty(), Optional.empty(), Optional.empty(), null);
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.of(new IdpSession(SESSION_ID, Instant.now(), idaAuthnRequestFromHubOptional, RELAY_STATE, null, null, null, null, null, null)));
        when(idpStubsRepository.getIdpWithFriendlyId(IDP_NAME)).thenReturn(idp);
        when(idp.userExists(USERNAME)).thenReturn(false);
        when(idp.createUser(any(), any(), any(), any(), any(), any(), any(), eq(USERNAME), any(), any())).thenReturn(mock(DatabaseIdpUser.class));

        idpUserService.createAndAttachIdpUserToSession(IDP_NAME, "bob", "jones", "address line 1", "address line 2", "address town", "address postcode", AuthnContext.LEVEL_2, "2000-01-01", Optional.empty(), USERNAME, "password", SESSION_ID);

        verify(sessionRepository, times(1)).updateSession(eq(SESSION_ID), any());
    }
}
