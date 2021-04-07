package stubsp.stubsp.resources;

import stubsp.stubsp.Urls;
import stubsp.stubsp.domain.SamlResponseFromIdpDto;
import stubsp.stubsp.services.SamlResponseService;
import stubsp.stubsp.views.AuthenticationFailedView;
import stubsp.stubsp.views.SuccessView;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path(Urls.SAML_SSO_RESPONSE_RESOURCE)
@Produces(MediaType.TEXT_HTML)
public class SamlResponseResource {

    private final SamlResponseService samlResponseService;

    @Inject
    public SamlResponseResource(SamlResponseService samlResponseService) {
        this.samlResponseService = samlResponseService;
    }

    @POST
    public Response receiveSamlResponse(@FormParam(Urls.SAML_RESPONSE_PARAM) String samlResponse,
                                        @FormParam(Urls.RELAY_STATE_PARAM) String relayState) {
        SamlResponseFromIdpDto samlResponseFromIdpDto = samlResponseService.processResponse(samlResponse, relayState);
        switch (samlResponseFromIdpDto.getResponseStatus()) {
            case SUCCESS -> {
                return Response.ok(new SuccessView(samlResponseFromIdpDto)).build();
            }
            case AUTHENTICATION_FAILED -> {
                return Response.ok(new AuthenticationFailedView(samlResponseFromIdpDto)).build();
            }
            default -> {
                return Response.seeOther(UriBuilder.fromPath(Urls.ROOT_RESOURCE).build())
                        .build();
            }
        }
    }
}
