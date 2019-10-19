package stubidp.stubidp.resources.eidas;

import com.google.common.base.Strings;
import stubidp.shared.csrf.CSRFCheckProtection;
import stubidp.shared.domain.SamlResponse;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.exceptions.GenericStubIdpException;
import stubidp.stubidp.exceptions.InvalidEidasSchemeException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountry;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.stubidp.services.EidasAuthnResponseService;
import stubidp.stubidp.services.StubCountryService;
import stubidp.stubidp.views.EidasLoginPageView;
import stubidp.stubidp.views.ErrorMessageType;
import stubidp.stubidp.views.SignAssertions;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import static java.text.MessageFormat.format;

@Path(Urls.EIDAS_LOGIN_RESOURCE)
@SessionCookieValueMustExistAsASession
@CSRFCheckProtection
public class EidasLoginPageResource {

    private final EidasSessionRepository sessionRepository;
    private final EidasAuthnResponseService eidasSuccessAuthnResponseRequest;
    private final SamlMessageRedirectViewFactory samlMessageRedirectViewFactory;
    private final StubCountryRepository stubCountryRepository;
    private final StubCountryService stubCountryService;

    @Inject
    public EidasLoginPageResource(
            EidasSessionRepository sessionRepository,
            EidasAuthnResponseService eidasSuucessAuthnResponseRequest,
            SamlMessageRedirectViewFactory samlMessageRedirectViewFactory,
            StubCountryRepository stubCountryRepository,
            StubCountryService stubCountryService) {
        this.sessionRepository = sessionRepository;
        this.eidasSuccessAuthnResponseRequest = eidasSuucessAuthnResponseRequest;
        this.samlMessageRedirectViewFactory = samlMessageRedirectViewFactory;
        this.stubCountryRepository = stubCountryRepository;
        this.stubCountryService = stubCountryService;
    }

    @GET
    public Response get(
            @PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeName,
            @QueryParam(Urls.ERROR_MESSAGE_PARAM) java.util.Optional<ErrorMessageType> errorMessage,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        final Optional<EidasScheme> eidasScheme = EidasScheme.fromString(schemeName);
        if(eidasScheme.isEmpty()) {
            throw new InvalidEidasSchemeException();
        }

        final EidasSession session = checkSession(schemeName, sessionCookie);

        StubCountry stubCountry = stubCountryRepository.getStubCountryWithFriendlyId(eidasScheme.get());

        sessionRepository.updateSession(session.getSessionId(), session.setNewCsrfToken());

        return Response.ok()
                .entity(new EidasLoginPageView(stubCountry.getDisplayName(), stubCountry.getFriendlyId(), errorMessage.orElse(ErrorMessageType.NO_ERROR).getMessage(), stubCountry.getAssetId(), session.getCsrfToken()))
                .build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeName,
            @FormParam(Urls.USERNAME_PARAM) String username,
            @FormParam(Urls.PASSWORD_PARAM) String password,
            @FormParam(Urls.SIGN_ASSERTIONS_PARAM) Optional<SignAssertions> signAssertionChecks,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        final boolean signAssertions = signAssertionChecks.isPresent() && SignAssertions.signAssertions.equals(signAssertionChecks.get());

        final Optional<EidasScheme> eidasScheme = EidasScheme.fromString(schemeName);
        if(eidasScheme.isEmpty()) {
            throw new InvalidEidasSchemeException();
        }

        EidasSession session = checkSession(schemeName, sessionCookie);

        try {
            stubCountryService.attachStubCountryToSession(eidasScheme.get(), username, password, signAssertions, session);
        } catch (InvalidUsernameOrPasswordException e) {
            return createErrorResponse(ErrorMessageType.INVALID_USERNAME_OR_PASSWORD, schemeName);
        } catch (InvalidSessionIdException e) {
            return createErrorResponse(ErrorMessageType.INVALID_SESSION_ID, schemeName);
        }

        return Response.seeOther(UriBuilder.fromPath(Urls.EIDAS_CONSENT_RESOURCE)
                .build(schemeName))
                .build();
    }

    @POST
    @Path(Urls.LOGIN_AUTHN_FAILURE_PATH)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postAuthnFailure(
            @PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeName,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        if(EidasScheme.fromString(schemeName).isEmpty()) {
            throw new InvalidEidasSchemeException();
        }

        EidasSession session = checkAndDeleteAndGetSession(schemeName, sessionCookie);

            final SamlResponse loginFailureResponse = eidasSuccessAuthnResponseRequest.generateAuthnFailed(session, schemeName);
            return samlMessageRedirectViewFactory.sendSamlResponse(loginFailureResponse);
    }

    private EidasSession checkSession(String schemeId, SessionId sessionCookie) {
        if (sessionCookie == null || Strings.isNullOrEmpty(sessionCookie.toString())) {
            throw new GenericStubIdpException(format("Unable to locate session cookie for " + schemeId), Response.Status.BAD_REQUEST);
        }

        Optional<EidasSession> session = sessionRepository.get(sessionCookie);

        if (session.isEmpty()) {
            throw new GenericStubIdpException(format("Session is invalid for " + schemeId), Response.Status.BAD_REQUEST);
        }

        return session.get();
    }

    private EidasSession checkAndDeleteAndGetSession(String schemeId, SessionId sessionCookie) {
        if (Strings.isNullOrEmpty(sessionCookie.toString())) {
            throw new GenericStubIdpException(format("Unable to locate session cookie for " + schemeId), Response.Status.BAD_REQUEST);
        }

        Optional<EidasSession> session = sessionRepository.deleteAndGet(sessionCookie);

        if (session.isEmpty()) {
            throw new GenericStubIdpException(format("Session is invalid for " + schemeId), Response.Status.BAD_REQUEST);
        }
        return session.get();
    }

    private Response createErrorResponse(ErrorMessageType errorMessage, String stubCountry) {
        URI uri = UriBuilder.fromPath(Urls.EIDAS_LOGIN_RESOURCE)
                .queryParam(Urls.ERROR_MESSAGE_PARAM, errorMessage)
                .build(stubCountry);
        return Response.seeOther(uri).build();
    }

}
