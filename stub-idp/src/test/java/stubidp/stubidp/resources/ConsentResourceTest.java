package stubidp.stubidp.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.utils.core.domain.AddressFactory;
import stubidp.shared.cookies.CookieNames;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.resources.idp.ConsentResource;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.services.SuccessAuthnResponseService;
import stubidp.stubidp.views.ConsentView;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.rest.common.SessionId;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsentResourceTest {

    private static final String RELAY_STATE = "relay";

    @Mock
    private IdpStubsRepository idpStubsRepository;
    @Mock
    private IdpSessionRepository sessionRepository;
    @Mock
    private IdaAuthnRequestFromHub idaAuthnRequestFromHub;
    @Mock
    private SuccessAuthnResponseService successAuthnResponseService;
    @Mock
    private NonSuccessAuthnResponseService nonSuccessAuthnResponseService;
    @Mock
    private CookieNames cookieNames;

    private ConsentResource consentResource;

    @BeforeEach
    public void setUp() {
        consentResource = new ConsentResource(idpStubsRepository, sessionRepository, successAuthnResponseService, nonSuccessAuthnResponseService, new SamlMessageRedirectViewFactory(cookieNames));
    }

    private final String idpName = "idpName";
    private final Idp idp = new Idp(idpName, "Test Idp", "test-idp-asset-id", true, TestEntityIds.STUB_IDP_ONE, null);

    @Test
    public void shouldWarnUserIfLOAIsTooLow() {
        final SessionId idpSessionId = SessionId.createNewSessionId();

        IdpSession session = new IdpSession(idpSessionId, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null);
        session.setIdpUser(newUser(AuthnContext.LEVEL_1));
        when(sessionRepository.get(idpSessionId)).thenReturn(Optional.of(session));

        when(idaAuthnRequestFromHub.getLevelsOfAssurance()).thenReturn(Collections.singletonList(AuthnContext.LEVEL_2));
        when(idpStubsRepository.getIdpWithFriendlyId(idpName)).thenReturn(idp);

        final Response response = consentResource.get(idpName, idpSessionId);

        final ConsentView consentView = (ConsentView) response.getEntity();
        assertThat(consentView.isUserLOADidNotMatch()).isTrue();
    }

    @Test
    public void shouldWarnUserIfLOAIsTooLowWhenMultipleValuesPresent() {
        final SessionId idpSessionId = SessionId.createNewSessionId();

        IdpSession session = new IdpSession(idpSessionId, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null);
        session.setIdpUser(newUser(AuthnContext.LEVEL_1));
        when(sessionRepository.get(idpSessionId)).thenReturn(Optional.of(session));

        when(idaAuthnRequestFromHub.getLevelsOfAssurance()).thenReturn(List.of(AuthnContext.LEVEL_1, AuthnContext.LEVEL_2));
        when(idpStubsRepository.getIdpWithFriendlyId(idpName)).thenReturn(idp);

        final Response response = consentResource.get(idpName, idpSessionId);

        final ConsentView consentView = (ConsentView) response.getEntity();
        assertThat(consentView.isUserLOADidNotMatch()).isFalse();
    }

    @Test
    public void shouldNotWarnUserIfLOAIsOk() {
        final SessionId idpSessionId = SessionId.createNewSessionId();

        IdpSession session = new IdpSession(idpSessionId, idaAuthnRequestFromHub, RELAY_STATE, null, null, null, null, null, null);
        session.setIdpUser(newUser(AuthnContext.LEVEL_2));
        when(sessionRepository.get(idpSessionId)).thenReturn(Optional.of(session));

        when(idaAuthnRequestFromHub.getLevelsOfAssurance()).thenReturn(Collections.singletonList(AuthnContext.LEVEL_2));
        when(idpStubsRepository.getIdpWithFriendlyId(idpName)).thenReturn(idp);

        final Response response = consentResource.get(idpName, idpSessionId);
        final ConsentView consentView = (ConsentView) response.getEntity();

        assertThat(consentView.isUserLOADidNotMatch()).isFalse();
    }

    private Optional<DatabaseIdpUser> newUser(AuthnContext levelOfAssurance) {
        return Optional.of(new DatabaseIdpUser(
                idpName + "-new",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(createMdsValue("Jack")),
                Collections.emptyList(),
                Collections.singletonList(createMdsValue("Griffin")),
                Optional.of(createMdsValue(Gender.NOT_SPECIFIED)),
                Collections.singletonList(createMdsValue(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of("1983-06-21"))),
                Collections.singletonList(new AddressFactory().createNoDates(Collections.singletonList("Lion's Head Inn"), "1A 2BC", null, null, true)),
                levelOfAssurance));
    }

    private static <T> SimpleMdsValue<T> createMdsValue(T value) {
        return (value == null) ? null : new SimpleMdsValue<>(value, null, null, true);
    }
}
