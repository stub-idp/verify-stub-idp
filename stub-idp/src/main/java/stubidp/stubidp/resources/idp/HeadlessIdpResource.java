package stubidp.stubidp.resources.idp;

import io.prometheus.client.Counter;
import org.opensaml.saml.saml2.core.AuthnRequest;
import stubidp.saml.stubidp.stub.transformers.inbound.AuthnRequestToIdaRequestFromHubTransformer;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.shared.domain.SamlResponse;
import stubidp.shared.views.SamlMessageRedirectViewFactory;
import stubidp.stubidp.Urls;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpStubsRepository;
import stubidp.stubidp.saml.IdpAuthnRequestValidator;
import stubidp.stubidp.services.SuccessAuthnResponseService;
import stubidp.utils.rest.common.SessionId;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path(Urls.HEADLESS_ROOT)
public class HeadlessIdpResource {

    public static final String IDP_NAME = "headless";
    private static final String STUBIDP_HEADLESS_RECEIVED_AUTHN_REQUESTS_TOTAL = "stubidp_headless_receivedAuthnRequests_total";
    private static final String STUBIDP_HEADLESS_SUCCESSFUL_AUTHN_REQUESTS_TOTAL = "stubidp_headless_successfulAuthnRequests_total";

    public static final Counter receivedHeadlessAuthnRequests = Counter.build()
            .name(STUBIDP_HEADLESS_RECEIVED_AUTHN_REQUESTS_TOTAL)
            .help("Number of received headless authn requests.")
            .register();
    public static final Counter successfulHeadlessAuthnRequests = Counter.build()
            .name(STUBIDP_HEADLESS_SUCCESSFUL_AUTHN_REQUESTS_TOTAL)
            .help("Number of successful headless authn requests.")
            .register();

    private final IdpAuthnRequestValidator idpAuthnRequestValidator;
    private final AuthnRequestToIdaRequestFromHubTransformer authnRequestToIdaRequestFromHubTransformer;
    private final IdpStubsRepository idpStubsRepository;
    private final SuccessAuthnResponseService successAuthnResponseService;
    private final SamlMessageRedirectViewFactory samlMessageRedirectViewFactory;

    @Inject
    public HeadlessIdpResource(
            IdpAuthnRequestValidator idpAuthnRequestValidator,
            AuthnRequestToIdaRequestFromHubTransformer authnRequestToIdaRequestFromHubTransformer,
            IdpStubsRepository idpStubsRepository,
            SuccessAuthnResponseService successAuthnResponseService,
            SamlMessageRedirectViewFactory samlMessageRedirectViewFactory) {

        this.idpAuthnRequestValidator = idpAuthnRequestValidator;
        this.authnRequestToIdaRequestFromHubTransformer = authnRequestToIdaRequestFromHubTransformer;
        this.idpStubsRepository = idpStubsRepository;
        this.successAuthnResponseService = successAuthnResponseService;
        this.samlMessageRedirectViewFactory = samlMessageRedirectViewFactory;
    }

    @POST
    public Response handlePost(
            @FormParam(Urls.SAML_REQUEST_PARAM) @NotNull String samlRequest,
            @FormParam(Urls.RELAY_STATE_PARAM) String relayState,
            @FormParam(Urls.CYCLE3_PARAM) boolean isC3,
            @Context HttpServletRequest httpServletRequest) {

        receivedHeadlessAuthnRequests.inc();

        final String username = isC3?"headless-c3":"headless";
        final AuthnRequest authnRequest = idpAuthnRequestValidator.transformAndValidate(IDP_NAME, samlRequest);
        final IdaAuthnRequestFromHub idaRequestFromHub = authnRequestToIdaRequestFromHubTransformer.apply(authnRequest);

        successfulHeadlessAuthnRequests.inc();

        final Optional<DatabaseIdpUser> idpUser = idpStubsRepository.getIdpWithFriendlyId(IDP_NAME).getUser(username, "bar");

        final IdpSession session = new IdpSession(SessionId.createNewSessionId(), idaRequestFromHub, relayState, null, null, null, null, null, null);
        session.setIdpUser(idpUser);

        final SamlResponse successResponse = successAuthnResponseService.getSuccessResponse(false, httpServletRequest.getRemoteAddr(), IDP_NAME, session);
        return samlMessageRedirectViewFactory.sendSamlResponse(successResponse);
    }

}
