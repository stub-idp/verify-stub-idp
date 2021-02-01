package uk.gov.ida.rp.testrp.authentication;

import com.google.common.base.Strings;
import io.dropwizard.auth.AuthenticationException;
import org.apache.commons.lang3.EnumUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.utils.rest.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.domain.JourneyHint;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.tokenservice.TokenService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import static uk.gov.ida.rp.testrp.Urls.Cookies.TEST_RP_SESSION_COOKIE_NAME;
import static uk.gov.ida.rp.testrp.Urls.Params.EIDAS_PARAM;
import static uk.gov.ida.rp.testrp.Urls.Params.FAIL_ACCOUNT_CREATION;
import static uk.gov.ida.rp.testrp.Urls.Params.JOURNEY_HINT_PARAM;
import static uk.gov.ida.rp.testrp.Urls.Params.NO_MATCH;
import static uk.gov.ida.rp.testrp.Urls.Params.RP_NAME_PARAM;
import static uk.gov.ida.rp.testrp.tokenservice.AccessTokenCookieName.ACCESS_TOKEN_COOKIE_NAME;

public class SessionFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SessionFactory.class);

    private final TestRpConfiguration configuration;
    private final SimpleAuthenticator authenticator;
    private final AuthnRequestSenderHandler authnRequestManager;
    private final String rpName;
    private final TokenService tokenService;
    private final ContainerRequest containerRequest;

    public SessionFactory(
            SimpleAuthenticator authenticator,
            TestRpConfiguration configuration,
            AuthnRequestSenderHandler authnRequestManager,
            TokenService tokenService,
            ContainerRequest containerRequest) {
        this.authenticator = authenticator;
        this.configuration = configuration;
        this.authnRequestManager = authnRequestManager;
        this.tokenService = tokenService;
        this.containerRequest = containerRequest;
        this.rpName = "test-rp";
    }

    public Session provide() {
        Optional<AccessToken> accessToken = Optional.empty();
        Map<String, Cookie> cookieNameValueMap = containerRequest.getCookies();
        if (cookieNameValueMap != null && cookieNameValueMap.containsKey(ACCESS_TOKEN_COOKIE_NAME)) {
            accessToken = Optional.of(new AccessToken(cookieNameValueMap.get(ACCESS_TOKEN_COOKIE_NAME).getValue()));
        }

        tokenService.validate(accessToken);

        Optional<String> overriddenRpName = getQueryParam(RP_NAME_PARAM);
        Optional<JourneyHint> journeyHint = Optional.empty();

        boolean forceAuthentication = configuration.getForceAuthentication();

        // Always force authentication when using the no-cycle-3 test RP
        if(overriddenRpName.isPresent()) {
            forceAuthentication = overriddenRpName.get().equals("test-rp-noc3");
        }

        final Optional<String> rawJourneyHint = getQueryParam(JOURNEY_HINT_PARAM);
        if (rawJourneyHint.isPresent()) {
            if (EnumUtils.isValidEnum(JourneyHint.class, rawJourneyHint.get())) {
                journeyHint = Optional.ofNullable(JourneyHint.valueOf(rawJourneyHint.get()));
            } else {
                journeyHint = Optional.empty();
            }
        }

        if (containsQueryParam(EIDAS_PARAM) && !journeyHint.isPresent()) {
            journeyHint = Optional.of(JourneyHint.eidas_sign_in);
        }

        return getUser(
            accessToken,
            overriddenRpName,
            journeyHint,
            forceAuthentication,
            containsQueryParam(NO_MATCH),
            containsQueryParam(FAIL_ACCOUNT_CREATION));
    }

    private Optional<String> getQueryParam(String queryParam) {
        String value = containerRequest.getUriInfo().getQueryParameters().getFirst(queryParam);
        return Strings.isNullOrEmpty(value) ? Optional.empty() : Optional.of(value);
    }

    private boolean containsQueryParam(String queryParam) {
        return containerRequest.getUriInfo().getQueryParameters().containsKey(queryParam);
    }

    public Session getUser(
            Optional<AccessToken> accessToken,
            Optional<String> overriddenRpName,
            Optional<JourneyHint> journeyHint,
            boolean forceAuthentication,
            boolean forceLMSNoMatch,
            boolean forceLMSUserAccountCreationFail) {

        try {
            Map<String, Cookie> cookieNameValueMap = containerRequest.getCookies();
            if (cookieNameValueMap != null && cookieNameValueMap.containsKey(TEST_RP_SESSION_COOKIE_NAME)) {
                Optional<String> sessionId = Optional.ofNullable(cookieNameValueMap.get(TEST_RP_SESSION_COOKIE_NAME).getValue());
                if (sessionId.isPresent()) {
                    Optional<Session> result = authenticator.authenticate(new SessionId(sessionId.get()));
                    if (result.isPresent() && !(journeyHint.isPresent() && EnumUtils.isValidEnum(JourneyHint.class, journeyHint.get().name()))) {
                        return result.get();
                    }
                }
            }
        } catch (AuthenticationException e) {
            LOG.warn("Error authenticating credentials", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        Response response = authnRequestManager.sendAuthnRequest(
                containerRequest.getUriInfo().getRequestUri(),
                Optional.empty(),
                overriddenRpName.orElse(rpName),
                accessToken,
                journeyHint,
                forceAuthentication,
                forceLMSNoMatch,
                forceLMSUserAccountCreationFail);

        throw new WebApplicationException(response);
    }
}
