package stubidp.stubidp.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.stubidp.domain.DatabaseEidasUser;
import stubidp.stubidp.domain.EidasAuthnRequest;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.exceptions.IncompleteRegistrationException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.exceptions.UsernameAlreadyTakenException;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountry;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.utils.rest.common.SessionId;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StubCountryServiceTest {

    private final String RELAY_STATE = "relayState";
    private static final EidasScheme EIDAS_SCHEME = EidasScheme.stub_country;
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private final SessionId SESSION_ID = SessionId.createNewSessionId();

    private StubCountryService stubCountryService;

    private Optional<DatabaseEidasUser> user;

    private EidasSession session;

    private EidasAuthnRequest eidasAuthnRequest;
    
    @Mock
    private EidasSessionRepository sessionRepository;

    @Mock
    private StubCountryRepository stubCountryRepository;

    @Mock
    private StubCountry stubCountry;

    @BeforeEach
    void setUp() {
        stubCountryService = new StubCountryService(stubCountryRepository, sessionRepository);
        eidasAuthnRequest = new EidasAuthnRequest("request-id", "issuer", "destination", "loa", Collections.emptyList());
        session = new EidasSession(SESSION_ID, Instant.now(), eidasAuthnRequest, null, null, null, Optional.empty(), Optional.empty());
        user = newUser();
    }

    @Test
    void shouldAttachEidasToSession() throws InvalidUsernameOrPasswordException, InvalidSessionIdException {
        when(stubCountry.getUser(USERNAME, PASSWORD)).thenReturn(user);
        when(stubCountryRepository.getStubCountryWithFriendlyId(EIDAS_SCHEME)).thenReturn(stubCountry);

        stubCountryService.attachStubCountryToSession(EIDAS_SCHEME, USERNAME, PASSWORD, true, session);

        assertThat(session.getEidasUser().isPresent()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotPresent() {
        user = Optional.empty();
        when(stubCountry.getUser(USERNAME, PASSWORD)).thenReturn(user);
        when(stubCountryRepository.getStubCountryWithFriendlyId(EIDAS_SCHEME)).thenReturn(stubCountry);

        Assertions.assertThrows(InvalidUsernameOrPasswordException.class, () -> stubCountryService.attachStubCountryToSession(EIDAS_SCHEME, USERNAME, PASSWORD, true, session));
    }

    @Test
    void shouldHaveStatusSuccessResponseWhenUserRegisters() throws InvalidSessionIdException, IncompleteRegistrationException, UsernameAlreadyTakenException, InvalidUsernameOrPasswordException {
        EidasSession session = new EidasSession(SESSION_ID, Instant.now(), eidasAuthnRequest, "test-relay-state", Collections.emptyList(), Collections.emptyList(), Optional.empty(), Optional.empty());
        when(stubCountryRepository.getStubCountryWithFriendlyId(EIDAS_SCHEME)).thenReturn(stubCountry);
        when(stubCountry.createUser(eq(USERNAME), eq(PASSWORD), any(), any(), any(), any(), any(), any())).thenReturn(newUser().get());

        stubCountryService.createAndAttachIdpUserToSession(EIDAS_SCHEME, USERNAME, "password", session, "bob", "bobNonLatin", "jones", "jonesNonLatin", "2000-01-01", AuthnContext.LEVEL_2);

        verify(sessionRepository, times(1)).updateSession(eq(SESSION_ID), any());
    }

    private Optional<DatabaseEidasUser> newUser() {
        return Optional.of(new DatabaseEidasUser("stub-country", UUID.randomUUID().toString(), "bar", createMdsValue("Jack"), Optional.of(createMdsValue("JackNonLatin")), createMdsValue("Griffin"), Optional.of(createMdsValue("GriffinNonLatin")), createMdsValue(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1983-06-21")), AuthnContext.LEVEL_2));
    }

    private static <T> SimpleMdsValue<T> createMdsValue(T value) {
        return (value == null) ? null : new SimpleMdsValue<>(value, null, null, true);
    }
}
