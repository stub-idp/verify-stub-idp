package stubidp.stubidp.resources.eidas;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.shared.csrf.CSRFCheckProtection;
import stubidp.shared.domain.SamlResponse;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.domain.SubmitButtonValue;
import stubidp.stubidp.exceptions.GenericStubIdpException;
import stubidp.stubidp.exceptions.IncompleteRegistrationException;
import stubidp.stubidp.exceptions.InvalidDateException;
import stubidp.stubidp.exceptions.InvalidEidasSchemeException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.exceptions.UsernameAlreadyTakenException;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountry;
import stubidp.stubidp.repositories.StubCountryRepository;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.services.StubCountryService;
import stubidp.stubidp.views.EidasRegistrationPageView;
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
import java.util.Objects;
import java.util.Optional;

import static java.text.MessageFormat.format;

@Path(Urls.EIDAS_REGISTER_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@SessionCookieValueMustExistAsASession
@CSRFCheckProtection
public class EidasRegistrationPageResource {

    private final StubCountryRepository stubsCountryRepository;
    private final StubCountryService stubCountryService;
    private final SamlMessageRedirectViewFactory samlMessageRedirectViewFactory;
    private final NonSuccessAuthnResponseService nonSuccessAuthnResponseService;
    private final EidasSessionRepository sessionRepository;

    @Inject
    public EidasRegistrationPageResource(
            StubCountryRepository stubsCountryRepository,
            StubCountryService stubCountryService,
            SamlMessageRedirectViewFactory samlMessageRedirectViewFactory,
            NonSuccessAuthnResponseService nonSuccessAuthnResponseService,
            EidasSessionRepository sessionRepository) {
        this.stubCountryService = stubCountryService;
        this.stubsCountryRepository = stubsCountryRepository;
        this.samlMessageRedirectViewFactory = samlMessageRedirectViewFactory;
        this.nonSuccessAuthnResponseService = nonSuccessAuthnResponseService;
        this.sessionRepository = sessionRepository;
    }

    @GET
    public Response get(
            @PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeId,
            @QueryParam(Urls.ERROR_MESSAGE_PARAM) Optional<ErrorMessageType> errorMessage,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        final Optional<EidasScheme> eidasScheme = EidasScheme.fromString(schemeId);
        if(eidasScheme.isEmpty()) {
            throw new InvalidEidasSchemeException();
        }

        if (Objects.isNull(sessionCookie.toString()) || sessionCookie.toString().isBlank()) {
            throw new GenericStubIdpException(format(("Unable to locate session cookie for " + schemeId)), Response.Status.BAD_REQUEST);
        }

        if (!sessionRepository.containsSession(sessionCookie)) {
            throw new GenericStubIdpException(format(("Session is invalid for " + schemeId)), Response.Status.BAD_REQUEST);
        }

        EidasSession session = sessionRepository.get(sessionCookie).get();

        sessionRepository.updateSession(session.getSessionId(), session.setNewCsrfToken());

        StubCountry stubCountry = stubsCountryRepository.getStubCountryWithFriendlyId(eidasScheme.get());
        return Response.ok(new EidasRegistrationPageView(stubCountry.getDisplayName(), stubCountry.getFriendlyId(), errorMessage.orElse(ErrorMessageType.NO_ERROR).getMessage(), stubCountry.getAssetId(), session.getCsrfToken())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @PathParam(Urls.SCHEME_ID_PARAM) @NotNull String schemeId,
            @FormParam(Urls.FIRSTNAME_PARAM) String firstname,
            @FormParam(Urls.NON_LATIN_FIRSTNAME_PARAM) String nonLatinFirstname,
            @FormParam(Urls.SURNAME_PARAM) String surname,
            @FormParam(Urls.NON_LATIN_SURNAME_PARAM) String nonLatinSurname,
            @FormParam(Urls.DATE_OF_BIRTH_PARAM) String dateOfBirth,
            @FormParam(Urls.USERNAME_PARAM) String username,
            @FormParam(Urls.PASSWORD_PARAM) String password,
            @FormParam(Urls.LEVEL_OF_ASSURANCE_PARAM) AuthnContext levelOfAssurance,
            @FormParam(Urls.SUBMIT_PARAM) @NotNull SubmitButtonValue submitButtonValue,
            @FormParam(Urls.SIGN_ASSERTIONS_PARAM) Optional<SignAssertions> signAssertionChecks,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        final boolean signAssertions = signAssertionChecks.isPresent() && SignAssertions.signAssertions.equals(signAssertionChecks.get());

        final Optional<EidasScheme> eidasScheme = EidasScheme.fromString(schemeId);
        if(eidasScheme.isEmpty()) {
            throw new InvalidEidasSchemeException();
        }

        if (Objects.isNull(sessionCookie.toString()) || sessionCookie.toString().isBlank()) {
            throw new GenericStubIdpException(format(("Unable to locate session cookie for " + schemeId)), Response.Status.BAD_REQUEST);
        }

        Optional<EidasSession> session = sessionRepository.get(sessionCookie);

        if (session.isEmpty()) {
            throw new GenericStubIdpException(format(("Session is invalid for " + schemeId)), Response.Status.BAD_REQUEST);
        }

        final String samlRequestId = session.get().getEidasAuthnRequest().getRequestId();

        switch (submitButtonValue) {
            case Cancel: {

                session = sessionRepository.deleteAndGet(sessionCookie);

                final SamlResponse cancelResponse = nonSuccessAuthnResponseService.generateAuthnCancel(schemeId, samlRequestId, session.get().getRelayState());
                return samlMessageRedirectViewFactory.sendSamlResponse(cancelResponse);
            }
            case Register: {
                try {
                    session.get().setSignAssertions(signAssertions);
                    stubCountryService.createAndAttachIdpUserToSession(
                            eidasScheme.get(),
                            username,
                            password,
                            session.get(),
                            firstname,
                            nonLatinFirstname,
                            surname,
                            nonLatinSurname,
                            dateOfBirth,
                            levelOfAssurance
                    );
                    return Response.seeOther(UriBuilder.fromPath(Urls.EIDAS_CONSENT_RESOURCE)
                            .build(schemeId))
                            .build();
                } catch (InvalidSessionIdException e) {
                    return createErrorResponse(ErrorMessageType.INVALID_SESSION_ID, schemeId);
                } catch (IncompleteRegistrationException e) {
                    return createErrorResponse(ErrorMessageType.INCOMPLETE_REGISTRATION, schemeId);
                } catch (InvalidDateException e) {
                    return createErrorResponse(ErrorMessageType.INVALID_DATE, schemeId);
                } catch (UsernameAlreadyTakenException e) {
                    return createErrorResponse(ErrorMessageType.USERNAME_ALREADY_TAKEN, schemeId);
                } catch (InvalidUsernameOrPasswordException e) {
                    return createErrorResponse(ErrorMessageType.INVALID_USERNAME_OR_PASSWORD, schemeId);
                }
            }
            default: {
                throw new GenericStubIdpException("invalid submit button value", Response.Status.BAD_REQUEST);
            }
        }
    }

    private Response createErrorResponse(ErrorMessageType errorMessage, String schemeId) {
        URI uri = UriBuilder.fromPath(Urls.EIDAS_REGISTER_RESOURCE)
                .queryParam(Urls.ERROR_MESSAGE_PARAM, errorMessage)
                .build(schemeId);
        return Response.seeOther(uri).build();
    }
}
