package uk.gov.ida.rp.testrp.controllogic;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.config.InitializationException;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.utils.rest.common.SessionId;
import uk.gov.ida.rp.testrp.domain.ResponseFromHub;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.opensaml.core.config.InitializationService.initialize;
import static uk.gov.ida.rp.testrp.builders.AttributeBuilder.anAttribute;

@ExtendWith(MockitoExtension.class)
public class AuthnResponseReceiverHandlerTest {

    @BeforeAll
    public static void beforeClass() throws InitializationException {
        initialize();
    }

    private static final String RESPONSE_ID = "responseId";
    private static final String ISSUER = "issuer";
    private static final URI DESTINATION = URI.create("dest");
    private static final String REQUEST_ID = "requestId";
    private static final String samlResponse = "samlResponse";
    private static final SessionId SESSION_ID = SessionId.createNewSessionId();
    private static final Session SESSION = new Session(
            SESSION_ID,
            REQUEST_ID,
            URI.create("pathUserWasTryingToAccess"),
            ISSUER,
            Optional.of(1),
            Optional.empty(),
            false,
            false,
            false);

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private Function<String, InboundResponseFromHub> samlResponseDeserialiser;

    private AuthnResponseReceiverHandler authnResponseReceiverHandler;

    @BeforeEach
    public void before() {
        authnResponseReceiverHandler = new AuthnResponseReceiverHandler(sessionRepository, samlResponseDeserialiser);
    }

    @Test
    public void testSuccessResponse() {
        InboundResponseFromHub inboundResponseFromHub = new InboundResponseFromHub(RESPONSE_ID, Instant.now(), REQUEST_ID, ISSUER, DESTINATION, Optional.empty(), TransactionIdaStatus.Success, Optional.of(new PersistentId("persistentId")), Optional.of(AuthnContext.LEVEL_2));
        when(samlResponseDeserialiser.apply(samlResponse)).thenReturn(inboundResponseFromHub);
        when(sessionRepository.getSession(SESSION_ID)).thenReturn(Optional.ofNullable(SESSION));

        final ResponseFromHub responseFromHub = authnResponseReceiverHandler.handleResponse(samlResponse, Optional.ofNullable(SESSION_ID));

        assertThat(responseFromHub.getTransactionIdaStatus()).isEqualTo(TransactionIdaStatus.Success);
        assertThat(responseFromHub.getAttributes()).isEmpty();
        assertThat(responseFromHub.getAuthnContext().isPresent()).isTrue();
        assertThat(responseFromHub.getSession().isPresent()).isTrue();
        assertThat(responseFromHub.getSessionId().isPresent()).isTrue();
        assertThat(responseFromHub.getRedirectUri().isPresent()).isTrue();
    }

    @Test
    public void testAccountCreationResponse() {
        final List<Attribute> attributes = Collections.unmodifiableList(Arrays.asList(
                anAttribute().withFirstName("bob"),
                anAttribute().withSurname("obo"),
                anAttribute().withAddressHistory(Arrays.asList("Aviation House", "Whitechapel building")
        )));
        InboundResponseFromHub inboundResponseFromHub = new InboundResponseFromHub(RESPONSE_ID, Instant.now(), REQUEST_ID, ISSUER, DESTINATION, Optional.ofNullable(attributes), TransactionIdaStatus.Success, Optional.of(new PersistentId("persistentId")), Optional.of(AuthnContext.LEVEL_2));
        when(samlResponseDeserialiser.apply(samlResponse)).thenReturn(inboundResponseFromHub);
        when(sessionRepository.getSession(SESSION_ID)).thenReturn(Optional.ofNullable(SESSION));

        final ResponseFromHub responseFromHub = authnResponseReceiverHandler.handleResponse(samlResponse, Optional.ofNullable(SESSION_ID));

        assertThat(responseFromHub.getTransactionIdaStatus()).isEqualTo(TransactionIdaStatus.Success);
        assertThat(responseFromHub.getAttributes()).hasSameSizeAs(attributes);
        assertThat(responseFromHub.getAuthnContext().isPresent()).isTrue();
        assertThat(responseFromHub.getSession().isPresent()).isTrue();
        assertThat(responseFromHub.getSessionId().isPresent()).isTrue();
        assertThat(responseFromHub.getRedirectUri().isPresent()).isFalse();
    }

    @Test
    public void testNonSuccessResponse() {
        InboundResponseFromHub inboundResponseFromHub = new InboundResponseFromHub(RESPONSE_ID, Instant.now(), REQUEST_ID, ISSUER, DESTINATION, Optional.empty(), TransactionIdaStatus.NoAuthenticationContext, Optional.empty(), Optional.empty());
        when(samlResponseDeserialiser.apply(samlResponse)).thenReturn(inboundResponseFromHub);

        final ResponseFromHub responseFromHub = authnResponseReceiverHandler.handleResponse(samlResponse, Optional.empty());

        assertThat(responseFromHub.getTransactionIdaStatus()).isEqualTo(TransactionIdaStatus.NoAuthenticationContext);
        assertThat(responseFromHub.getAttributes()).isEmpty();
        assertThat(responseFromHub.getAuthnContext().isPresent()).isFalse();
        assertThat(responseFromHub.getSession().isPresent()).isFalse();
        assertThat(responseFromHub.getSessionId().isPresent()).isFalse();
        assertThat(responseFromHub.getRedirectUri().isPresent()).isFalse();
    }


}
