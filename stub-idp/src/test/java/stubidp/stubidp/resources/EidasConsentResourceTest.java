package stubidp.stubidp.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.domain.EidasAuthnRequest;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.domain.EidasUser;
import stubidp.stubidp.domain.SamlResponseFromValue;
import stubidp.stubidp.exceptions.GenericStubIdpException;
import stubidp.stubidp.exceptions.InvalidSigningAlgorithmException;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountry;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.stubidp.resources.eidas.EidasConsentResource;
import stubidp.stubidp.services.EidasAuthnResponseService;
import stubidp.utils.rest.common.SessionId;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EidasConsentResourceTest {

    private EidasConsentResource resource;

    private final String SCHEME_NAME = EidasScheme.stub_country.getEidasSchemeName();
    private final SessionId SESSION_ID = SessionId.createNewSessionId();
    private EidasSession session;

    @Mock
    private EidasSessionRepository sessionRepository;

    @Mock
    private EidasAuthnResponseService rsaSha256AuthnResponseService;

    @Mock
    private EidasAuthnResponseService rsaSsaPssAuthnResponseService;

    @Mock
    private SamlMessageRedirectViewFactory samlResponseRedirectViewFactory;

    @Mock
    private StubCountryRepository stubCountryRepository;

    @Mock
    private StubCountry stubCountry;

    @BeforeEach
    public void setUp(){
        resource = new EidasConsentResource(sessionRepository, rsaSha256AuthnResponseService, rsaSsaPssAuthnResponseService, samlResponseRedirectViewFactory, stubCountryRepository);

        EidasAuthnRequest eidasAuthnRequest = new EidasAuthnRequest("request-id", "issuer", "destination", "loa", Collections.emptyList());
        session = new EidasSession(SESSION_ID, eidasAuthnRequest, null, null, null, null, null);
        EidasUser user = new EidasUser("Jane", Optional.empty(), "Doe", Optional.empty(), "pid", LocalDate.of(1990, 1, 2).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant(), null, null);
        session.setEidasUser(user);
    }

    @Test
    public void getShouldReturnASuccessfulResponseWhenSessionIsValid() {
        when(sessionRepository.get(SESSION_ID)).thenReturn(Optional.of(session));
        when(stubCountryRepository.getStubCountryWithFriendlyId(EidasScheme.fromString(SCHEME_NAME).get())).thenReturn(stubCountry);

        final Response response = resource.get(SCHEME_NAME, SESSION_ID);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void postShouldReturnASuccessfulResponseWithRsaSha256SigningAlgorithmWhenSessionIsValid() {
        when(sessionRepository.deleteAndGet(SESSION_ID)).thenReturn(Optional.of(session));
        SamlResponseFromValue<org.opensaml.saml.saml2.core.Response> samlResponse = new SamlResponseFromValue<org.opensaml.saml.saml2.core.Response>(null, (r) -> null, null, null);
        when(rsaSha256AuthnResponseService.getSuccessResponse(session, SCHEME_NAME)).thenReturn(samlResponse);
        when(samlResponseRedirectViewFactory.sendSamlResponse(samlResponse)).thenReturn(Response.ok().build());

        final Response response = resource.consent(SCHEME_NAME, "rsasha256","submit", SESSION_ID);

        verify(rsaSsaPssAuthnResponseService, never()).getSuccessResponse(session, SCHEME_NAME);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void postShouldReturnASuccessfulResponseWithRsaSsaPsaSigningAlgorithmWhenSessionIsValid() {
        when(sessionRepository.deleteAndGet(SESSION_ID)).thenReturn(Optional.of(session));
        SamlResponseFromValue<org.opensaml.saml.saml2.core.Response> samlResponse = new SamlResponseFromValue<org.opensaml.saml.saml2.core.Response>(null, (r) -> null, null, null);
        when(rsaSsaPssAuthnResponseService.getSuccessResponse(session, SCHEME_NAME)).thenReturn(samlResponse);
        when(samlResponseRedirectViewFactory.sendSamlResponse(samlResponse)).thenReturn(Response.ok().build());

        final Response response = resource.consent(SCHEME_NAME, "rsassa-pss","submit", SESSION_ID);

        verify(rsaSha256AuthnResponseService, never()).getSuccessResponse(session, SCHEME_NAME);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void postShouldThrowAnExceptionWhenAnInvalidSigningAlgorithmIsUsed() {
        Assertions.assertThrows(InvalidSigningAlgorithmException.class, () -> resource.consent(SCHEME_NAME, "rsa-sha384","submit", SESSION_ID));
    }

    @Test
    public void shouldThrowAGenericStubIdpExceptionWhenSessionIsEmpty() {
        Assertions.assertThrows(GenericStubIdpException.class, () -> resource.get(SCHEME_NAME, null));
    }

}
