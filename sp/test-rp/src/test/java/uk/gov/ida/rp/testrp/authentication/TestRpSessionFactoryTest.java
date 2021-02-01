package uk.gov.ida.rp.testrp.authentication;


import io.dropwizard.auth.AuthenticationException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.rest.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.rp.testrp.domain.JourneyHint;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.tokenservice.TokenService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.rp.testrp.Urls.Cookies.TEST_RP_SESSION_COOKIE_NAME;

@ExtendWith(MockitoExtension.class)
public class TestRpSessionFactoryTest {

    private SessionFactory factory;

    @Mock
    private TestRpConfiguration configuration;

    @Mock
    private SimpleAuthenticator authenticator;

    @Mock
    private AuthnRequestSenderHandler authnRequestManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private ResourceContext resourceContext;

    @Mock
    private ContainerRequest containerRequest;

    @Mock
    private ExtendedUriInfo uriInfo;

    private final Session expectedSession = new Session(
            SessionId.createNewSessionId(),
            "requestId",
            URI.create("pathUserWasTryingToAccess"),
            "issuerId",
            Optional.of(1),
            Optional.empty(),
            false,
            false,
            false);

    @BeforeEach
    public void before() throws Exception {
        when(containerRequest.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());


        factory = new SessionFactory(authenticator,
                configuration,
                authnRequestManager,
                tokenService,
                containerRequest);
    }

    @Test
    public void shouldProvideASession() throws AuthenticationException {
        Map<String, Cookie> cookieMap = new HashMap<>();
        Cookie theUserCookieValue = new Cookie(TEST_RP_SESSION_COOKIE_NAME, expectedSession.getSessionId().getSessionId());
        cookieMap.put(TEST_RP_SESSION_COOKIE_NAME, theUserCookieValue);
        when(containerRequest.getCookies()).thenReturn(cookieMap);
        when(authenticator.authenticate(any())).thenReturn(Optional.of(expectedSession));

        Session session = factory.provide();
        assertThat(session).isEqualTo(expectedSession);
    }

    @Test
    public void shouldSendAuthnRequestWithEidasFlagWhenQueryStringContainsEidas() throws URISyntaxException {
        MultivaluedHashMap<String, String> queryParams = new MultivaluedHashMap<>();
        queryParams.put("eidas", singletonList("true"));

        when(uriInfo.getQueryParameters()).thenReturn(queryParams);
        when(uriInfo.getRequestUri()).thenReturn(new URI("http://uri"));
        when(containerRequest.getUriInfo()).thenReturn(uriInfo);

        Response response = null;
        try {
            factory.provide();
        } catch (WebApplicationException wae){
            response = wae.getResponse();
        }
        assertThat(response).isNotNull();

        verify(authnRequestManager).sendAuthnRequest(any(URI.class),
                any(),
                anyString(),
                any(),
                eq(Optional.of(JourneyHint.eidas_sign_in)),
                anyBoolean(),
                anyBoolean(),
                anyBoolean()
        );
    }

}
