package stubidp.stubidp.resources.idp;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.shared.csrf.CSRFCheckProtection;
import stubidp.shared.domain.SamlResponse;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.domain.SubmitButtonValue;
import stubidp.stubidp.exceptions.GenericStubIdpException;
import stubidp.stubidp.exceptions.IncompleteRegistrationException;
import stubidp.stubidp.exceptions.InvalidDateException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.exceptions.UsernameAlreadyTakenException;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.services.IdpUserService;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.views.ErrorMessageType;
import stubidp.stubidp.views.RegistrationPageView;
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

@Path(Urls.IDP_REGISTER_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@CSRFCheckProtection
public class RegistrationPageResource {

    private final IdpStubsRepository idpStubsRepository;
    private final IdpUserService idpUserService;
    private final SamlMessageRedirectViewFactory samlMessageRedirectViewFactory;
    private final NonSuccessAuthnResponseService nonSuccessAuthnResponseService;
    private final IdpSessionRepository idpSessionRepository;

    @Inject
    public RegistrationPageResource(
            IdpStubsRepository idpStubsRepository,
            IdpUserService idpUserService,
            SamlMessageRedirectViewFactory samlMessageRedirectViewFactory,
            NonSuccessAuthnResponseService nonSuccessAuthnResponseService,
            IdpSessionRepository idpSessionRepository) {
        this.idpUserService = idpUserService;
        this.idpStubsRepository = idpStubsRepository;
        this.samlMessageRedirectViewFactory = samlMessageRedirectViewFactory;
        this.nonSuccessAuthnResponseService = nonSuccessAuthnResponseService;
        this.idpSessionRepository = idpSessionRepository;
    }

    @GET
    @SessionCookieValueMustExistAsASession
    public Response get(
            @PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
            @QueryParam(Urls.ERROR_MESSAGE_PARAM) Optional<ErrorMessageType> errorMessage,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) @NotNull SessionId sessionCookie) {

        final IdpSession session = checkAndGetSession(idpName, sessionCookie);

        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);

        idpSessionRepository.updateSession(session.getSessionId(), session.setNewCsrfToken());

        return Response.ok(new RegistrationPageView(idp.getDisplayName(), idp.getFriendlyId(), errorMessage.orElse(ErrorMessageType.NO_ERROR).getMessage(), idp.getAssetId(), false, session.getCsrfToken())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
            @FormParam(Urls.FIRSTNAME_PARAM) String firstname,
            @FormParam(Urls.SURNAME_PARAM) String surname,
            @FormParam(Urls.ADDRESS_LINE1_PARAM) String addressLine1,
            @FormParam(Urls.ADDRESS_LINE2_PARAM) String addressLine2,
            @FormParam(Urls.ADDRESS_TOWN_PARAM) String addressTown,
            @FormParam(Urls.ADDRESS_POST_CODE_PARAM) String addressPostCode,
            @FormParam(Urls.DATE_OF_BIRTH_PARAM) String dateOfBirth,
            @FormParam(Urls.INCLUDE_GENDER_PARAM) boolean includeGender,
            @FormParam(Urls.GENDER_PARAM) Gender gender,
            @FormParam(Urls.USERNAME_PARAM) String username,
            @FormParam(Urls.PASSWORD_PARAM) String password,
            @FormParam(Urls.LEVEL_OF_ASSURANCE_PARAM) AuthnContext levelOfAssurance,
            @FormParam(Urls.SUBMIT_PARAM) @NotNull SubmitButtonValue submitButtonValue,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) SessionId sessionCookie) {

        if(Objects.isNull(sessionCookie)) {
            return createErrorResponse(ErrorMessageType.INVALID_SESSION_ID, idpName);
        }

        if (Objects.isNull(sessionCookie.toString()) || sessionCookie.toString().isBlank()) {
            throw new GenericStubIdpException(format("Unable to locate session cookie for " + idpName), Response.Status.BAD_REQUEST);
        }

        Optional<IdpSession> session = idpSessionRepository.get(sessionCookie);

        if (session.isEmpty()) {
            throw new GenericStubIdpException(format("Session is invalid for " + idpName), Response.Status.BAD_REQUEST);
        }

        if (session.get().getIdaAuthnRequestFromHub() == null) {
            return preRegisterResponse(idpName, firstname, surname, addressLine1, addressLine2, addressTown, addressPostCode, dateOfBirth, includeGender?Optional.ofNullable(gender):Optional.empty(), username, password, levelOfAssurance, submitButtonValue, sessionCookie);
        } else {
            return registerResponse(idpName, firstname, surname, addressLine1, addressLine2, addressTown, addressPostCode, dateOfBirth, includeGender?Optional.ofNullable(gender):Optional.empty(), username, password, levelOfAssurance, submitButtonValue, sessionCookie, session);
        }
    }

    private Response preRegisterResponse(String idpName,
                                         String firstname,
                                         String surname,
                                         String addressLine1,
                                         String addressLine2,
                                         String addressTown,
                                         String addressPostCode,
                                         String dateOfBirth,
                                         Optional<Gender> gender,
                                         String username,
                                         String password,
                                         AuthnContext levelOfAssurance,
                                         SubmitButtonValue submitButtonValue,
                                         SessionId sessionCookie) {
        switch (submitButtonValue) {
            case Cancel: {
                idpSessionRepository.deleteSession(sessionCookie);
                return Response.seeOther(UriBuilder.fromPath(Urls.SINGLE_IDP_CANCEL_PRE_REGISTER_RESOURCE).build(idpName)).build();
            }
            case Register: {
                try {
                    idpUserService.createAndAttachIdpUserToSession(idpName,
                            firstname, surname, addressLine1, addressLine2, addressTown, addressPostCode,
                            levelOfAssurance, dateOfBirth, gender, username, password, sessionCookie);
                } catch (InvalidSessionIdException e) {
                    return createErrorResponse(ErrorMessageType.INVALID_SESSION_ID, idpName);
                } catch (IncompleteRegistrationException e) {
                    return createErrorResponse(ErrorMessageType.INCOMPLETE_REGISTRATION, idpName);
                } catch (InvalidDateException e) {
                    return createErrorResponse(ErrorMessageType.INVALID_DATE, idpName);
                } catch (UsernameAlreadyTakenException e) {
                    return createErrorResponse(ErrorMessageType.USERNAME_ALREADY_TAKEN, idpName);
                } catch (InvalidUsernameOrPasswordException e) {
                    return createErrorResponse(ErrorMessageType.INVALID_USERNAME_OR_PASSWORD, idpName);
                }

                return Response.seeOther(UriBuilder.fromPath(Urls.SINGLE_IDP_START_PROMPT_RESOURCE)
                        .queryParam(Urls.SOURCE_PARAM,Urls.SOURCE_PARAM_PRE_REG_VALUE)
                        .build(idpName))
                        .build();
            }
            default: {
                throw new GenericStubIdpException("unknown submit button value", Response.Status.BAD_REQUEST);
            }
        }

    }

    private Response registerResponse(String idpName,
                                  String firstname,
                                  String surname,
                                  String addressLine1,
                                  String addressLine2,
                                  String addressTown,
                                  String addressPostCode,
                                  String dateOfBirth,
                                  Optional<Gender> gender,
                                  String username,
                                  String password,
                                  AuthnContext levelOfAssurance,
                                  SubmitButtonValue submitButtonValue,
                                  SessionId sessionCookie,
                                  Optional<IdpSession> session) {
        final String samlRequestId = session.get().getIdaAuthnRequestFromHub().getId();

        switch (submitButtonValue) {
            case Cancel: {

                session = idpSessionRepository.deleteAndGet(sessionCookie);

                final SamlResponse cancelResponse = nonSuccessAuthnResponseService.generateAuthnCancel(idpName, samlRequestId, session.get().getRelayState());
                return samlMessageRedirectViewFactory.sendSamlResponse(cancelResponse);
            }
            case Register: {
                try {
                    idpUserService.createAndAttachIdpUserToSession(idpName, firstname, surname, addressLine1, addressLine2, addressTown, addressPostCode, levelOfAssurance, dateOfBirth, gender, username, password, sessionCookie);
                    return Response.seeOther(UriBuilder.fromPath(Urls.IDP_CONSENT_RESOURCE)
                            .build(idpName))
                            .build();
                } catch (InvalidSessionIdException e) {
                    return createErrorResponse(ErrorMessageType.INVALID_SESSION_ID, idpName);
                } catch (IncompleteRegistrationException e) {
                    return createErrorResponse(ErrorMessageType.INCOMPLETE_REGISTRATION, idpName);
                } catch (InvalidDateException e) {
                    return createErrorResponse(ErrorMessageType.INVALID_DATE, idpName);
                } catch (UsernameAlreadyTakenException e) {
                    return createErrorResponse(ErrorMessageType.USERNAME_ALREADY_TAKEN, idpName);
                } catch (InvalidUsernameOrPasswordException e) {
                    return createErrorResponse(ErrorMessageType.INVALID_USERNAME_OR_PASSWORD, idpName);
                }
            }
            default: {
                throw new GenericStubIdpException("unknown submit button value", Response.Status.BAD_REQUEST);
            }
        }
    }

    private IdpSession checkAndGetSession(String idpName, SessionId sessionCookie) {
        if (Objects.isNull(sessionCookie.toString()) || sessionCookie.toString().isBlank()) {
            throw new GenericStubIdpException(format("Unable to locate session cookie for " + idpName), Response.Status.BAD_REQUEST);
        }

        Optional<IdpSession> session = idpSessionRepository.get(sessionCookie);

        if (session.isEmpty()) {
            throw new GenericStubIdpException(format("Session is invalid for " + idpName), Response.Status.BAD_REQUEST);
        }

        return session.get();
    }

    private Response createErrorResponse(ErrorMessageType errorMessage, String idpName) {
        URI uri = UriBuilder.fromPath(Urls.IDP_REGISTER_RESOURCE)
                .queryParam(Urls.ERROR_MESSAGE_PARAM, errorMessage)
                .build(idpName);
        return Response.seeOther(uri).build();
    }
}
