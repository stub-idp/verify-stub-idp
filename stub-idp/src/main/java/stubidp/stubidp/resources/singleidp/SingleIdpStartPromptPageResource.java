package stubidp.stubidp.resources.singleidp;

import stubidp.stubidp.Urls;
import stubidp.stubidp.configuration.SingleIdpConfiguration;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.Service;
import stubidp.stubidp.exceptions.FeatureNotEnabledException;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASession;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.services.ServiceListService;
import stubidp.stubidp.views.ErrorMessageType;
import stubidp.stubidp.views.SingleIdpPromptPageView;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path(Urls.SINGLE_IDP_START_PROMPT_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@SessionCookieValueMustExistAsASession
public class SingleIdpStartPromptPageResource {

    private final IdpStubsRepository idpStubsRepository;
    private final ServiceListService serviceListService;
    private final SingleIdpConfiguration singleIdpConfiguration;
    private final IdpSessionRepository idpSessionRepository;

    @Inject
    public SingleIdpStartPromptPageResource(
            IdpStubsRepository idpStubsRepository,
            ServiceListService serviceListService,
            SingleIdpConfiguration singleIdpConfiguration,
            IdpSessionRepository idpSessionRepository) {
        this.idpStubsRepository = idpStubsRepository;
        this.serviceListService = serviceListService;
        this.singleIdpConfiguration = singleIdpConfiguration;
        this.idpSessionRepository = idpSessionRepository;
    }

    @GET
    public Response get(
            @PathParam(Urls.IDP_ID_PARAM) @NotNull String idpName,
            @QueryParam(Urls.ERROR_MESSAGE_PARAM) Optional<ErrorMessageType> errorMessage,
            @QueryParam(Urls.SOURCE_PARAM) Optional<String> source,
            @CookieParam(StubIdpCookieNames.SESSION_COOKIE_NAME) SessionId sessionCookie) throws FeatureNotEnabledException {
        if (!singleIdpConfiguration.isEnabled()) throw new FeatureNotEnabledException();
        Idp idp = idpStubsRepository.getIdpWithFriendlyId(idpName);
        UUID uuid = UUID.randomUUID();
        Optional<DatabaseIdpUser> idpUser = Optional.empty();

        List<Service> serviceList = serviceListService.getServices();
        if (source.isPresent() && source.get().equals(Urls.SOURCE_PARAM_PRE_REG_VALUE) && sessionCookie != null) {
            Optional<IdpSession> idpSession = idpSessionRepository.get(sessionCookie);
            if(idpSession.isPresent()) {
                idpUser = idpSession.get().getIdpUser();
            }
        }

        return Response.ok()
            .entity(
                new SingleIdpPromptPageView(idp.getDisplayName(),
                    idp.getIssuerId(),
                    errorMessage.orElse(ErrorMessageType.NO_ERROR).getMessage(),
                    idp.getAssetId(),
                    serviceList,
                    singleIdpConfiguration.getVerifySubmissionUri(),
                    uuid,
                    idpUser.orElse(null)
                )
            )
            .build();
    }
}
